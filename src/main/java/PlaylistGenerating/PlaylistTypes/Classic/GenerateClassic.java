package PlaylistGenerating.PlaylistTypes.Classic;

import ExceptionClasses.ArtistExceptions.GetSeveralArtistsException;
import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.PlaylistExceptions.AddItemsToPlaylistException;
import ExceptionClasses.PlaylistExceptions.CreatePlaylistException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import ExceptionClasses.TrackExceptions.GetAudioFeaturesForTrackException;
import PlaylistGenerating.PlaylistTypes.GeneratePlaylist;
import SpotifyUtilities.PlaylistUtilities;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.*;

import static PlaylistGenerating.PlaylistTypes.Classic.ClassicCheckingUtilities.*;
import static PlaylistGenerating.PlaylistTypes.Classic.ClassicTrackUtilities.*;
import static PlaylistGenerating.PlaylistTypes.CommonUtilities.*;
import static SpotifyUtilities.PlaylistUtilities.createPlaylist;
import static SpotifyUtilities.TrackUtilities.duration_comparator;

public class GenerateClassic extends GeneratePlaylist {

    protected static final int limit = 21; // Number of tracks we want to get
    protected static int num_intervals;
    protected static int transition_length_ms = 0; // transition length is the warm-up/ wind down sequence individually
    protected static int min_transition_length_ms;
    protected static int max_transition_length_ms;
    protected static int target_length_ms = 0;
    protected static int min_target_length_ms;
    protected static int max_target_length_ms;
    private final float bpm_difference;
    private HashMap<Integer, TrackSimplified[]> intervals;
    private final float transition_moe = .015f;
    private static final int bpm_offset = 3; // How far from the query bpm we want song tempos in the recommendations request below


    /**
     * Constructor for generating a classic style playlist
     *
     * @param spotify_api    SpotifyApi object that has been built with the current user's access token
     * @param genres         Desired Genres
     * @param age            Age of the user
     * @param workout_length Length of the workout
     * @param intensity      desired intensity (low, medium, high)
     */
    public GenerateClassic(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity,
                           boolean is_personalized) throws GetCurrentUsersProfileException,
            GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException, GetSeveralArtistsException {

        super(spotify_api, genres, age, workout_length, intensity, is_personalized);

        intervals = new HashMap<>();

        // warmup and wind-down are the same length and are 10% of the workout each
        float transition_length_min;

        if (workout_length > 30) {
            transition_length_min = workout_length * .1f;
        } else {
            transition_length_min = 3.5f; // If the workout is short the transition will take up more
        }

        // Number of intervals in which there is one song per interval (For warmup/wind-down only)
        // We will round up to avoid intervals needing to have exceptionally long songs
        num_intervals = Math.round(transition_length_min / avg_song_len);
        bpm_difference = findBpmDifference();

        transition_length_ms = (int) (transition_length_min * 60_000); // conversion
        setTransitionLengths(transition_moe);

        target_length_ms = workout_len_ms - (transition_length_ms * 2);
        setTargetLengths(margin_of_error);
    }

    @Override
    public String generatePlaylist() throws GetCurrentUsersProfileException, GetRecommendationsException,
            CreatePlaylistException, AddItemsToPlaylistException, GetAudioFeaturesForTrackException {

        TrackSimplified[] warmup_track_uris = findTransitionTracks(true);
        System.out.println("warmup");
        TrackSimplified[] target_track_uris = getTargetTracks();
        System.out.println("target");
        TrackSimplified[] wind_down_track_uris = findTransitionTracks(false);
        System.out.println("wind-down");

        TrackSimplified[] playlist_tracks = concatTracks(warmup_track_uris, target_track_uris, wind_down_track_uris);

        eliminateDupesAndNonPlayable(spotify_api, playlist_tracks, genres, seed_artists, seed_tracks, user.getCountry());

        // Create a playlist on the user's account
        Playlist playlist = createPlaylist(spotify_api, user.getId(), user.getDisplayName());
        String playlist_id = playlist.getId();
        String[] playlist_track_uris = getTrackURIs(playlist_tracks);

        System.out.println("Creating Playlist");
        PlaylistUtilities.addItemsToPlaylist(spotify_api, playlist_id, playlist_track_uris);

        return playlist_id;
    }

    //                                  //
    // Transition Tracks                //
    //                                  //

    /**
     * @param is_warmup boolean indication if this is for the warmup sequence
     * @return TrackSimplified array
     * @throws GetRecommendationsException if recommendations endpoint encounters an issue
     */
    private TrackSimplified[] findTransitionTracks(boolean is_warmup) throws GetRecommendationsException {

        TrackSimplified[] tracks = null;
        int closest_column;
        float local_moe = transition_moe; // Keeps track of moe for duration purposes which we will be altering here

        do {
            intervals = getSortedIntervals(is_warmup); // populated class variable intervals with sorted intervals
            closest_column = findClosestColumn();

            // If a good closest column was found try and find the best fit.

            if (closest_column != -1) {
                tracks = getBestFit(closest_column);
            }

            setTransitionLengths(local_moe += .005); // relax the moe a bit so we can find something

        } while (tracks == null); // if an acceptable ordering was not found, try again

        setTransitionLengths(transition_moe); // restore moe

        return tracks;
    }

    /**
     * Now that the closest column of songs has been found, preform some fine grain searching to find a good duration
     *
     * @param closest_column closest column of songs that was found and provided by the calling function
     * @return TrackSimplified array of the track IDS, or null if a good ordering could not be found
     */
    private TrackSimplified[] getBestFit(int closest_column) {

        TrackSimplified[] tracks = getTracksInColumn(intervals, closest_column);
        TrackSimplified[] adjacent_tracks; // Tracks that are shorter or longer than closest_column depending on result
        TrackSimplified[][] track_matrix;
        DURATION_RESULT result = checkTransitionDuration(tracks);

        // If the duration is acceptable go no further
        if (result == DURATION_RESULT.ACCEPTABLE) {
            return tracks;
        }

        // If there is only one interval there is only one song per column so doing fine grain combination
        // searching is redundant in this case.
        if(num_intervals < 1 ) return null;

        // Otherwise proceed with fine grain searching
        if (result == DURATION_RESULT.TOO_SHORT || result == DURATION_RESULT.WITHIN_THIRTY_SECONDS_SHORT) {

            if (closest_column < (limit - 1)) {
                adjacent_tracks = getTracksInColumn(intervals, closest_column + 1);
            } else return null; // if the longer tracks are out of bounds no combination will be suitable

        } else { // Too long or within 30 seconds too long
            if (closest_column > 0) {
                adjacent_tracks = getTracksInColumn(intervals, closest_column - 1);
            } else return null; // if the shorter tracks are out of bounds no combination will be suitable
        }

        track_matrix = new TrackSimplified[num_intervals][2];

        // Populate the track matrix
        for (int row = 0; row < num_intervals; row++) {
            track_matrix[row][0] = tracks[row];
            track_matrix[row][1] = adjacent_tracks[row];
        }

        tracks = tryTrackCombinations(track_matrix, new TrackSimplified[num_intervals], 0);

        return tracks;
    }

    /**
     * recursive function that tries all track combinations to find a combination that fits in the allowable time range
     *
     * @param matrix 2d matrix of the closest column and its appropriate adjacent column
     * @param tracks tracks that are currently being passed down the recursive call chain and will be altered at row
     * @param row    current row in the matrix to swap in the value of tracks
     * @return TrackSimplified array of acceptable tracks if found, null otherwise
     */
    private TrackSimplified[] tryTrackCombinations(TrackSimplified[][] matrix, TrackSimplified[] tracks, int row) {
        DURATION_RESULT result;
        TrackSimplified[] track_combo;

        if (row == matrix.length) {

            result = checkTransitionDuration(tracks);

            if (result == DURATION_RESULT.ACCEPTABLE) {
                return tracks;
            } else {
                return null;
            }
        }

        // index < 2 as there is always 2 columns to analyze
        for (int column = 0; column < 2; column++) {

            tracks[row] = matrix[row][column];

            track_combo = tryTrackCombinations(matrix, tracks, row + 1);

            // if the track_combo is null it means the final track array was not acceptable
            if (track_combo != null) {
                return track_combo;
            }
        }

        return null;
    }

    /**
     * Finds the column closest to the target duration
     *
     * @return the closest column or -1 if there was not a good column
     */
    private int findClosestColumn() {
        TrackSimplified[] current_tracks;

        // First check if the shortest combination is too long and check if the longest combination is too short
        current_tracks = getTracksInColumn(intervals, 0);
        if (checkTransitionDuration(current_tracks) == DURATION_RESULT.TOO_LONG) return -1;

        current_tracks = getTracksInColumn(intervals, limit - 1);
        if (checkTransitionDuration(current_tracks) == DURATION_RESULT.TOO_SHORT) return -1;

        int current_column = limit / 2; // middle column
        int current_scope = current_column; // portion of the limit we are looking at, will be halved repeatedly
        int new_column;
        int distance_to_previous_column = 100;
        int distance_to_bound = 100;
        DURATION_RESULT result = DURATION_RESULT.ACCEPTABLE;

        while (distance_to_previous_column >= 4 && distance_to_bound >= 3) {

            current_tracks = getTracksInColumn(intervals, current_column);
            result = checkTransitionDuration(current_tracks);

            if (result == DURATION_RESULT.TOO_SHORT) {
                new_column = (current_column + 1) + (current_scope / 2); // mid-point of the right side of the current

                distance_to_previous_column = new_column - current_column; // new - current since new will be greater
                distance_to_bound = (limit - 1) - new_column; // distance to right side of TrackSimplified array
            } else if (result == DURATION_RESULT.TOO_LONG) {
                new_column = (current_column - 1) - (current_scope / 2); // mid-point of the left side of the current

                distance_to_previous_column = current_column - new_column; // current - new since current is greater
                distance_to_bound = new_column; // distance from 0
            } else { // In this case the column is either an acceptable length or 30 seconds off from the target
                break;
            }

            current_column = new_column; // update the current column
            current_scope /= 2; // halve the current scope as our search area halves
        }
        // In the cases that we left the while loop due to being within 30 seconds of the allowable duration, or we
        // got too close to the bounds of the track arrays we are going to search adjacent columns for best fit

        if (result == DURATION_RESULT.TOO_SHORT || result == DURATION_RESULT.WITHIN_THIRTY_SECONDS_SHORT) {
            return checkForLongerColumn(intervals, current_column);
        } else if (result == DURATION_RESULT.TOO_LONG || result == DURATION_RESULT.WITHIN_THIRTY_SECONDS_LONG) {
            return checkForShorterColumn(intervals, current_column);
        } else if (result == DURATION_RESULT.ACCEPTABLE) {
            return current_column;
        } else {
            return -1;
        }
    }


    /**
     * Fetches songs in each interval of the warmup/wind down sequences and sorts each interval by duration
     *
     * @param is_warmup indicates if the desired sorted intervals to be returned should be for the warmup sequence
     * @return Hashmap of Integer keys representing each interval [0 - num_intervals) and TrackSimplified[] as values
     * @throws GetRecommendationsException if recommendation API call encounters an issue,
     *                                     will be tossed up to the calling function
     */
    private HashMap<Integer, TrackSimplified[]> getSortedIntervals(boolean is_warmup)
            throws GetRecommendationsException {


        HashMap<Integer, TrackSimplified[]> sorted_intervals = new HashMap<>();

        float query_bpm = initializeQueryBPM(is_warmup); // will be updated in for-loop

//        System.out.println("Resting BPM: " + resting_bpm);
//        System.out.println("Target BPM: " + target_bpm);
//        System.out.println("num_intervals: " + num_intervals);
//        System.out.println("BPM difference: " + bpm_difference);

        // We need to call the recommendations endpoint for each interval, fetching a few songs in that interval's range
        // This yields better results than requesting a lot of songs in a large range
        for (int current_interval = 0; current_interval < num_intervals; current_interval++) {

            //System.out.println("is_warmup: " + is_warmup);
            //System.out.println("query_bpm: " + query_bpm + '\n');

            // Update BPM to the next interval
            query_bpm = updateQueryBPM(query_bpm, is_warmup);

            //System.out.println(query_bpm);

            TrackSimplified[] recommended_tracks;

            int local_offset = bpm_offset;

            do {

                recommended_tracks = getSortedRecommendations(limit, query_bpm - local_offset,
                        query_bpm + local_offset, query_bpm);

                local_offset++;

            } while (recommended_tracks.length < limit);

            sorted_intervals.put(current_interval, recommended_tracks);
        }

        return sorted_intervals;
    }


    //                                  //
    // Target Tracks                    //
    //                                  //

    /**
     * Gets the target tracks for the target sequence
     *
     * @return TrackSimplified array of the track IDs
     */
    private TrackSimplified[] getTargetTracks() throws GetRecommendationsException {

        int target_length_min = target_length_ms / 60_000;
        // number of tracks we want in the target sequence
        int num_tracks = Math.round(target_length_min / avg_song_len);
        HashSet<TrackSimplified> track_set = new HashSet<>();

        int local_offset = bpm_offset;
        int local_limit = num_tracks * 2;

        do {

            TrackSimplified[] recommended_tracks = getSortedRecommendations(local_limit,
                    target_bpm - local_offset, target_bpm + local_offset, target_bpm);


            // Hash set can take the null element which we want to avoid, also want to avoid adding process if empty
            if (recommended_tracks != null && recommended_tracks.length != 0) {
                track_set.addAll(List.of(recommended_tracks));
            }

            // If the limit has been met
            if (track_set.size() >= limit) {

                TrackSimplified[] track_array = track_set.toArray(TrackSimplified[]::new);

                TrackSimplified[] tracks = findBestTargetTracks(track_array, num_tracks);

                if (tracks != null){
                    Arrays.sort(tracks, duration_comparator); // Sort the tracks in ascending duration
                    return tracks;
                }


                track_set.clear(); // empty the track set and increase local limit to try again
                local_limit += 5;

                if(local_limit > 100) local_limit = 100;

            }

            local_offset++;

        } while (true);
    }

    /**
     * Grabs the first batch of songs from the beginning of the tracks array and shifts the selected group
     * of songs to the right until a song is found, or we reach a point where it is clear no grouping is acceptable
     *
     * @param tracks    TrackSimplified array to search for a good ordering of target songs
     * @param num_songs number of songs we want to gather and check against the desired duration
     * @return TrackSimplified array of songs that fit in the target duration window, null otherwise
     */
    private TrackSimplified[] findBestTargetTracks(TrackSimplified[] tracks, int num_songs) {

        Deque<TrackSimplified> deque = new ArrayDeque<>();
        int index = 0;
        DURATION_RESULT result;
        TrackSimplified current_track;

        // Get the first batch of songs into the deque
        for (; index < num_songs; index++) {
            current_track = tracks[index];
            deque.add(current_track);
        }

        result = checkTargetDuration(deque);

        if (result == DURATION_RESULT.ACCEPTABLE) return deque.toArray(TrackSimplified[]::new);
        // If the shortest combination of tracks is too long there is no suitable combination so return null
        if (result == DURATION_RESULT.TOO_LONG) return null;

        for (; index < tracks.length; index++) {

            current_track = tracks[index];

            // This essentially shifts the group of tracks we are analyzing to the right (longer) side
            deque.removeFirst(); // remove the shortest track
            deque.add(current_track); // add the next track in line

            result = checkTargetDuration(deque);

            if (result == DURATION_RESULT.ACCEPTABLE) return deque.toArray(TrackSimplified[]::new);

            if (result == DURATION_RESULT.TOO_LONG) return null; // If now too long there is no acceptable combination
        }

        return null;
    }

    //                                             //
    // Initializing / Updating Utilities           //
    //                                             //


    /**
     * Sets the min and max Target lengths based on the provided margin of error
     *
     * @param margin_of_error margin of error used in setting the min and max target lengths
     */
    private void setTargetLengths(float margin_of_error) {
        // MilliSeconds of length that is acceptable for the target sequence based on our MOE
        min_target_length_ms = target_length_ms - (int) (target_length_ms * margin_of_error);
        max_target_length_ms = target_length_ms + (int) (target_length_ms * margin_of_error);
    }

    /**
     * Sets the min and max transition lengths based on the provided margin of error
     *
     * @param margin_of_error margin of error used in setting the min and max transition lengths
     */
    private void setTransitionLengths(float margin_of_error) {

        // MilliSeconds of length that is acceptable for the warmup / wind-down sequence based on our MOE
        min_transition_length_ms = transition_length_ms - (int) (transition_length_ms * margin_of_error);
        max_transition_length_ms = transition_length_ms + (int) (transition_length_ms * margin_of_error);
    }

    /**
     * Initializes query_bpm based on isWarmup
     *
     * @param is_warmup indicates if we are in the warmup sequence or nor
     * @return resting_bpm if in warmup sequence, target bpm otherwise
     */
    private float initializeQueryBPM(boolean is_warmup) {
        if (is_warmup) {
            return resting_bpm; // If warming up we want to start from the resting BPM and go up
        } else {
            return target_bpm; // If winding down we want to start from the target BPM and come down
        }
    }

    /**
     * Updates the query bpm based on the isWarmup boolean
     *
     * @param query_bpm the bpm to be updated and returned
     * @param is_warmup if true query bpm will be increased otherwise decreased
     * @return updated query bpm
     */
    private float updateQueryBPM(float query_bpm, boolean is_warmup) {

        if (is_warmup) {
            return query_bpm + bpm_difference; // If warming up we want to increase the BPM
        } else {
            return query_bpm - bpm_difference; // If winding down we want to decrease the BPM
        }
    }

    /**
     * Calculate the bpm difference between each interval in the warmup/wind-down sequence
     *
     * @return (float) BPM difference between intervals
     */
    private float findBpmDifference() {
        // Special case for workouts less than 45 minutes, we want one interval with the bpm difference being halfway
        // between the resting bpm and target bpm. We also want to relax the MOE a bit here
        if (num_intervals < 2) {
            num_intervals = 1;
            setTransitionLengths(transition_moe);

            return (float) ((target_bpm - resting_bpm) / 2);
        } else {
            // By finding the difference between the target bpm and resting bpm and finally dividing by the number of
            // intervals in the warmup we find the tempo difference between each interval
            return (float) ((target_bpm - resting_bpm) / num_intervals);
        }
    }
}