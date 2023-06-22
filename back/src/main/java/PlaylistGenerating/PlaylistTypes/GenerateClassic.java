package PlaylistGenerating.PlaylistTypes;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.PlaylistExceptions.AddItemsToPlaylistException;
import ExceptionClasses.PlaylistExceptions.CreatePlaylistException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import PlaylistGenerating.TargetHeartRateRange;
import SpotifyUtilities.PlaylistUtilities;
import SpotifyUtilities.RecommendationArguments;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.*;
import java.util.stream.Stream;

import static PlaylistGenerating.DesiredHeartRateRanges.getTargetHeartRateRange;
import static SpotifyUtilities.BrowsingUtilities.getRecommendations;
import static SpotifyUtilities.PersonalizationUtilities.GetUsersTopArtists;
import static SpotifyUtilities.PersonalizationUtilities.GetUsersTopTracks;
import static SpotifyUtilities.PlaylistUtilities.createPlaylist;
import static SpotifyUtilities.TrackUtilities.duration_comparator;
import static SpotifyUtilities.TrackUtilities.getAudioFeaturesForSeveralTracks;
import static SpotifyUtilities.UserProfileUtilities.getCurrentUsersProfile;

public class GenerateClassic extends GeneratePlaylist {

    private final float transition_length_min; // minutes
    private final String seed_artists;
    private final String seed_tracks;
    private final int seed_genres_provided;
    private int desired_num_seed_artists;
    private int desired_num_seed_tracks;
    private final int target_bpm;
    private final float bpm_difference;
    private int num_intervals;
    private final int transition_length_ms; // transition refers to the length of the warm-up/ wind down sequence individually
    private int min_transition_length_ms;
    private int max_transition_length_ms;
    private final int target_length_ms;
    private int min_target_length_ms;
    private int max_target_length_ms;

    private final int bpm_offset = 3; // How far from the query bpm we want song tempos in the recommendations request below

    HashMap<Integer, TrackSimplified[]> intervals;

    private final int limit = 21; // Number of tracks we want to get

    private float transition_moe = .02f;

    private enum DURATION_RESULT {
        ACCEPTABLE, TOO_SHORT, TOO_LONG, WITHIN_THIRTY_SECONDS_SHORT, WITHIN_THIRTY_SECONDS_LONG
    }

    //TODO: check the bpms in the created playlists to ensure they are what we want

    /**
     * Constructor for generating a classic style playlist
     *
     * @param spotify_api    SpotifyApi object that has been built with the current user's access token
     * @param genres         Desired Genres
     * @param age            Age of the user
     * @param workout_length Length of the workout
     * @param intensity      desired intensity (low, medium, high)
     */
    public GenerateClassic(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity)
            throws Exception {

        super(spotify_api, genres, age, workout_length, intensity);

        intervals = new HashMap<>();

        target_bpm = getTargetBPM();

        determineSeedLimits();

        seed_artists = getSeedArtists();
        seed_tracks = getSeedTracks();

        // warmup and wind-down are the same length and are 10% of the workout each
        transition_length_min = workout_length * .1f;

        // Only 5 seed values across tracks, artists, and genres can be provided to the recommendations endpoint
        // so if less than the max of 3 genres the front end lets the user choose from were chosen we can provide
        // additional seed artists or seed tracks
        seed_genres_provided = (int) genres.chars().filter(ch -> ch == ',').count();

        // Number of intervals in which there is one song per interval (For warmup/wind-down only)
        // We will round up to avoid intervals needing to have exceptionally long songs
        num_intervals = Math.round(transition_length_min / 3.5f);

        bpm_difference = findBpmDifference();

        transition_length_ms = (int) (transition_length_min * 60_000); // conversion

        target_length_ms = workout_length_ms - (transition_length_ms * 2);

        // MilliSeconds of length that is acceptable for the target sequence based on our MOE
        min_target_length_ms = target_length_ms - (int) (target_length_ms * margin_of_error);
        max_target_length_ms = target_length_ms + (int) (target_length_ms * margin_of_error);

        setTransitionLengths(transition_moe);
    }

    @Override
    public String generatePlaylist() throws GetCurrentUsersProfileException, GetRecommendationsException,
            CreatePlaylistException, AddItemsToPlaylistException {

        User user = getCurrentUsersProfile(spotify_api);

        String[] warmup_track_uris = findTransitionTracks(true);
        System.out.println("warmup");
        String[] target_track_uris = getTargetTracks();
        System.out.println("target");
        String[] wind_down_track_uris = findTransitionTracks(false);
        System.out.println("wind-down");

        String[] playlist_track_uris = concatTracks(warmup_track_uris, target_track_uris, wind_down_track_uris);

//        try {
//
//            AudioFeatures[] features = getAudioFeaturesForSeveralTracks(spotify_api, playlist_track_uris);
//
//            for(AudioFeatures feature: features){
//                System.out.println(feature.getTempo());
//            }
//
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }

        // Create a playlist on the user's account
        Playlist playlist = createPlaylist(spotify_api, user.getId(), user.getDisplayName());

        String playlist_id = playlist.getId();

        System.out.println("Creating Playlist");
        PlaylistUtilities.addItemsToPlaylist(spotify_api, playlist_id, playlist_track_uris);

        return playlist_id;
    }

    @Override
    protected int getTargetBPM() {
        TargetHeartRateRange targetHeartRateRange = getTargetHeartRateRange(this.age);

        switch (intensity) {
            case "low" -> {
                return targetHeartRateRange.low_intensity_target();
            }
            case "medium" -> {
                return targetHeartRateRange.medium_intensity_target();
            }
            case "high" -> {
                return targetHeartRateRange.high_intensity_target();
            }
            default -> {
                return -1;
                // Due to input validation in GeneratePlaylistHandler this should not be reached
            }
        }
    }

    //                                  //
    // Transition Tracks                //
    //                                  //

    /**
     * @param is_warmup boolean indication if this is for the warmup sequence
     * @return String array of the Track IDs
     * @throws GetRecommendationsException if recommendations endpoint encounters an issue
     */
    private String[] findTransitionTracks(boolean is_warmup) throws GetRecommendationsException{

        String[] track_ids = null;
        int closest_column;
        float old_moe = transition_moe;

        do {
            intervals = getSortedIntervals(is_warmup); // populated class variable intervals with sorted intervals

            closest_column = findClosestColumn();

            if (closest_column == -1) continue; // a column close enough to the duration was not found so start again

            transition_moe += .01; // relax the moe a bit so we can find something
            setTransitionLengths(transition_moe);

            track_ids = getBestFit(closest_column);

        } while (track_ids == null); // if an acceptable ordering was not found, try again

        transition_moe = old_moe; // restore moe
        setTransitionLengths(transition_moe);

        return getBestFit(closest_column);
    }

    /**
     * Now that the closest column of songs has been found, preform some fine grain searching to find a good duration
     *
     * @param closest_column closest column of songs that was found and provided by the calling function
     * @return String array of the track IDS, or null if a good ordering could not be found
     */
    private String[] getBestFit(int closest_column) {

        TrackSimplified[] tracks = getTracksInColumn(closest_column);
        TrackSimplified[] adjacent_tracks; // Tracks that are shorter or longer than closest_column depending on result
        TrackSimplified[][] track_matrix;
        DURATION_RESULT result = checkTransitionDuration(tracks);

        if (result == DURATION_RESULT.ACCEPTABLE) {

            return getTrackIDs(tracks);
        } else if (result == DURATION_RESULT.TOO_SHORT || result == DURATION_RESULT.WITHIN_THIRTY_SECONDS_SHORT) {

            if (closest_column < (limit - 1)) {
                adjacent_tracks = getTracksInColumn(closest_column + 1);
            } else return null; // if the longer tracks are out of bounds no combination will be suitable

        } else { // Too long or within 30 seconds too long
            if (closest_column > 0) {
                adjacent_tracks = getTracksInColumn(closest_column - 1);
            } else return null; // if the shorter tracks are out of bounds no combination will be suitable
        }

        track_matrix = new TrackSimplified[num_intervals][2];

        // Populate the track matrix
        for (int row = 0; row < num_intervals; row++) {
            track_matrix[row][0] = tracks[row];
            track_matrix[row][1] = adjacent_tracks[row];
        }

        tracks = tryTrackCombinations(track_matrix, new TrackSimplified[num_intervals], 0);

        return getTrackIDs(tracks);
    }

    /**
     * recursive function that tries all track combinations to find a combination that fits in the allowable time range
     *
     * @param matrix 2d matrix of the closest column and its appropriate adjacent column
     * @param tracks tracks that are currently being passed down the recursive call chain and will be altered at row
     * @param row current row in the matrix to swap in the value of tracks
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
        current_tracks = getTracksInColumn(0);
        if (checkTransitionDuration(current_tracks) == DURATION_RESULT.TOO_LONG) return -1;

        current_tracks = getTracksInColumn(limit - 1);
        if (checkTransitionDuration(current_tracks) == DURATION_RESULT.TOO_SHORT) return -1;

        int current_column = limit / 2; // middle column
        int current_scope = current_column; // portion of the limit we are looking at, will be halved repeatedly
        int new_column;
        int distance_to_previous_column = 100;
        int distance_to_bound = 100;
        DURATION_RESULT result = DURATION_RESULT.ACCEPTABLE;

        while (distance_to_previous_column >= 4 && distance_to_bound >= 3) {

            current_tracks = getTracksInColumn(current_column);
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
            return checkForLongerColumn(current_column);
        } else if (result == DURATION_RESULT.TOO_LONG || result == DURATION_RESULT.WITHIN_THIRTY_SECONDS_LONG) {
            return checkForShorterColumn(current_column);
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
     * will be tossed up to the calling function
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

            }while (recommended_tracks.length < limit);

            sorted_intervals.put(current_interval, recommended_tracks);
        }

        return sorted_intervals;
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
     * Checks columns to the right of the given column in the TrackSimplified arrays in the intervals map for better
     * fitting columns
     *
     * @param column column to start from
     * @return best fitting column
     */
    private int checkForLongerColumn(int column) {

        TrackSimplified[] next_column_tracks;
        DURATION_RESULT result;

        do {

            // if the next column is out of bounds return the current column
            if (column + 1 >= limit) return column;

            next_column_tracks = getTracksInColumn(column + 1);

            result = checkTransitionDuration(next_column_tracks);

            // if the next column is a good fit, return it
            if (result == DURATION_RESULT.ACCEPTABLE) return column + 1;

            // if the result of the next column is now too long the current column is the most ideal
            if (result == DURATION_RESULT.TOO_LONG || result == DURATION_RESULT.WITHIN_THIRTY_SECONDS_LONG) {
                return column;
            } else {
                column++; // if the next column is not too long keep searching
            }

        } while (true);
    }

    /**
     * Checks columns to the left of the given column in the TrackSimplified arrays in the intervals map for better
     * fitting columns
     *
     * @param column column to start from
     * @return best fitting column
     */
    private int checkForShorterColumn(int column) {

        TrackSimplified[] next_column_tracks;
        DURATION_RESULT result;

        do {

            // if the next column is out of bounds return the current column
            if (column - 1 < 0) return column;

            next_column_tracks = getTracksInColumn(column - 1);

            result = checkTransitionDuration(next_column_tracks);

            // if the next column is a good fit, return it
            if (result == DURATION_RESULT.ACCEPTABLE) return column - 1;

            // if the result of the next column is now too short the current column is the most ideal
            if (result == DURATION_RESULT.TOO_SHORT || result == DURATION_RESULT.WITHIN_THIRTY_SECONDS_SHORT) {
                return column;
            } else {
                column--; // if the next column is not too long keep searching
            }

        } while (true);
    }

    //                                  //
    // Target Tracks                    //
    //                                  //

    /**
     * Gets the target tracks for the target sequence
     *
     * @return String array of the track IDs
     */
    private String[] getTargetTracks() throws GetRecommendationsException {

        int target_length_min = target_length_ms / 60_000;
        // number of tracks we want in the target sequence
        int num_tracks = Math.round(target_length_min / 3.5f);

        int local_offset = bpm_offset;

        do {

            TrackSimplified[] recommended_tracks = getSortedRecommendations(num_tracks * 2,
                    target_bpm - local_offset, target_bpm + local_offset, target_bpm);

            TrackSimplified[] tracks = findBestTargetTracks(recommended_tracks, num_tracks);

            if (tracks != null) return getTrackIDs(tracks);

            System.out.println("null");
            System.out.println(local_offset);

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

            if(result == DURATION_RESULT.ACCEPTABLE) return deque.toArray(TrackSimplified[]::new);

            if(result== DURATION_RESULT.TOO_LONG) return null; // If now too long there is no acceptable combination
        }

        return null;
    }

    //                                  //
    // Initializing Utilities           //
    //                                  //


    private void setTransitionLengths(float margin_of_error){

        // MilliSeconds of length that is acceptable for the warmup / wind-down sequence based on our MOE
        this.min_transition_length_ms = transition_length_ms - (int) (transition_length_ms * margin_of_error);
        this.max_transition_length_ms = transition_length_ms + (int) (transition_length_ms * margin_of_error);
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
     * Calculate the bpm difference between each interval in the warmup/wind-down sequence
     *
     * @return (float) BPM difference between intervals
     */
    private float findBpmDifference() {
        // Special case for workouts less than 45 minutes, we want one interval with the bpm difference being halfway
        // between the resting bpm and target bpm. We also want to relax the MOE a bit here
        if (num_intervals < 2) {
            num_intervals = 1;
            transition_moe = .15f;
            setTransitionLengths(transition_moe);

            return (float) ((target_bpm - resting_bpm) / 2);
        } else {
            // By finding the difference between the target bpm and resting bpm and finally dividing by the number of
            // intervals in the warmup we find the tempo difference between each interval
            return (float) ((target_bpm - resting_bpm) / num_intervals);
        }
    }

    /**
     * Based on the number of seed genres provided set the limits for seed artists and seed tracks.
     * This is important as the recommendation endpoint only allows 5 seed values in any combination
     * of genres, tracks, and artists
     */
    private void determineSeedLimits() {
        switch (seed_genres_provided) {
            case 1 -> {
                desired_num_seed_artists = 2;
                desired_num_seed_tracks = 2;
            }
            case 2 -> {
                desired_num_seed_artists = 1;
                desired_num_seed_tracks = 2;
            }
            case 3 -> {
                desired_num_seed_artists = 1;
                desired_num_seed_tracks = 1;
            }
        }
    }

    /**
     * Gets the users top track(s) and returns a comma seperated string of their IDS
     *
     * @return comma seperated string of IDs of the users top track(s)
     * @throws GetUsersTopTracksRequestException if an error occurs fetching the users top tracks
     */
    private String getSeedTracks() throws GetUsersTopTracksRequestException {

        Track[] seed_tracks = GetUsersTopTracks(spotify_api, desired_num_seed_tracks);

        StringBuilder string_builder = new StringBuilder();

        // Will flip to true when there is more than one artist is seed_artists, so they can be comma seperated
        boolean flag = false;

        for (Track track : seed_tracks) {

            // This will be false for the first iteration preventing a leading comma but true for all the rest
            if (flag) {
                string_builder.append(",");
            }

            string_builder.append(track.getId());

            flag = true;
        }

        return string_builder.toString();
    }

    /**
     * Gets the users top artist(s) and returns a comma seperated string of their IDS
     *
     * @return comma seperated string of IDs of the users top artist(s)
     * @throws GetUsersTopArtistsRequestException if an error occurs fetching the users top artists
     */
    private String getSeedArtists() throws GetUsersTopArtistsRequestException {

        Artist[] seed_artists = GetUsersTopArtists(spotify_api, desired_num_seed_artists);

        StringBuilder string_builder = new StringBuilder();

        // Will flip to true when there is more than one artist is seed_artists, so they can be comma seperated
        boolean flag = false;

        for (Artist artist : seed_artists) {

            // This will be false for the first iteration preventing a leading comma but true for all the rest
            if (flag) {
                string_builder.append(",");
            }

            string_builder.append(artist.getId());

            flag = true;
        }

        return string_builder.toString();
    }


    //                                  //
    // Track and Playlist Utilities     //
    //                                  //

    /**
     * Loops through all the given tracks and stores their IDs in a string array which is then returned
     *
     * @param tracks array of tracks to fetch the id from
     * @return String array of all the given track's ids, returns null if tracks is null
     */
    private String[] getTrackIDs(TrackSimplified[] tracks) {

        if (tracks == null) return null;

        int size = tracks.length;

        String[] ids = new String[size];

        for (int index = 0; index < size; index++) {
            ids[index] = tracks[index].getUri();
            //ids[index] = tracks[index].getId();
        }

        return ids;
    }

    /**
     * Gets each track in the specified column from each interval in the provided intervals argument
     *
     * @param column desired column to fetch a track from each interval
     * @return TrackSimplified array of the songs from the requested column in each interval
     */
    private TrackSimplified[] getTracksInColumn(int column) {

        TrackSimplified[] return_tracks = new TrackSimplified[num_intervals];

        TrackSimplified[] current_interval_tracks;

        for (int interval = 0; interval < num_intervals; interval++) {
            current_interval_tracks = intervals.get(interval);

            return_tracks[interval] = current_interval_tracks[column];
        }

        return return_tracks;
    }

    /**
     * @param arrays array arguments to concat
     * @return String array of the concatenated arguments
     */
    private String[] concatTracks(String[]... arrays) {

        Stream<String> stream = Stream.of();

        for (String[] array : arrays) {
            stream = Stream.concat(stream, Arrays.stream(array));
        }

        return stream.toArray(String[]::new);

    }

    /**
     * Calls the recommendation endpoint and sorts the returned response
     *
     * @param limit number of songs to fetch
     * @param min_tempo min tempo of songs to fetch
     * @param max_tempo max tempo of songs to fetch
     * @param target_tempo target tempo of songs to fetch
     * @return TrackSimplified array of sorted tracks which were fetched by the recommendation endpoint
     * @throws GetRecommendationsException if an error occurs when fetching the recommendation
     */
    private TrackSimplified[] getSortedRecommendations(int limit, float min_tempo, float max_tempo, float target_tempo)
            throws GetRecommendationsException {

        RecommendationArguments current_arguments = new RecommendationArguments(
                spotify_api, limit, genres, seed_artists, seed_tracks,
                min_tempo, max_tempo, target_tempo);

        Recommendations recommendations = getRecommendations(current_arguments);

        TrackSimplified[] recommended_tracks = recommendations.getTracks();

        Arrays.sort(recommended_tracks, duration_comparator);

        return recommended_tracks;
    }

    //                                  //
    // Checking / verifying utilities   //
    //                                  //

    /**
     * Checks if the track array provided is within the allowable duration range
     * SPECIFICALLY FOR WARMUP AND WIND-DOWN SEQUENCES ONLY
     *
     * @param tracks tracks to be checked for their duration
     * @return appropriately named enum (TOO_SHORT if too short, TOO_LONG if too long, and ACCEPTABLE if acceptable)
     */
    private DURATION_RESULT checkTransitionDuration(TrackSimplified[] tracks) {
        int duration_ms = 0;

        for (TrackSimplified track : tracks) {
            duration_ms += track.getDurationMs();
        }

        System.out.println("Duration: " + duration_ms);
        System.out.println("Target Duration: " + transition_length_ms);
        System.out.println("Min Duration: " + min_transition_length_ms);
        System.out.println("Max Duration: " + max_transition_length_ms);

        int thirty_seconds_ms = 30_000;

        if (duration_ms < min_transition_length_ms && duration_ms >= min_transition_length_ms - thirty_seconds_ms) {
            return DURATION_RESULT.WITHIN_THIRTY_SECONDS_SHORT;
        } else if (duration_ms > max_transition_length_ms && duration_ms <= max_transition_length_ms + thirty_seconds_ms) {
            return DURATION_RESULT.WITHIN_THIRTY_SECONDS_LONG;
        } else if (duration_ms < min_transition_length_ms) {
            return DURATION_RESULT.TOO_SHORT;
        } else if (duration_ms > max_transition_length_ms) {
            return DURATION_RESULT.TOO_LONG;
        } else {
            return DURATION_RESULT.ACCEPTABLE;
        }
    }

    /**
     * Checks if the track array provided is within the allowable duration range
     * SPECIFICALLY FOR TARGET SEQUENCE ONLY
     *
     * @param tracks tracks to be checked for their duration
     * @return appropriately named enum (TOO_SHORT if too short, TOO_LONG if too long, and ACCEPTABLE if acceptable)
     */
    private DURATION_RESULT checkTargetDuration(Deque<TrackSimplified> tracks) {
        int duration_ms = 0;

        for (TrackSimplified track : tracks) {
            duration_ms += track.getDurationMs();
        }

        System.out.println("Duration: " + duration_ms);
        System.out.println("Target Duration: " + target_length_ms);
        System.out.println("Min Duration: " + min_target_length_ms);
        System.out.println("Max Duration: " + max_target_length_ms);

        if (duration_ms < min_target_length_ms) {
            return DURATION_RESULT.TOO_SHORT;
        } else if (duration_ms > max_target_length_ms) {
            return DURATION_RESULT.TOO_LONG;
        } else {
            return DURATION_RESULT.ACCEPTABLE;
        }
    }
}