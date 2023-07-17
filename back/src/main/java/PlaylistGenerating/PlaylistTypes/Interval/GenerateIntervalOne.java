package PlaylistGenerating.PlaylistTypes.Interval;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import ExceptionClasses.TrackExceptions.GetAudioFeaturesForTrackException;
import PlaylistGenerating.PlaylistTypes.GeneratePlaylist;
import SpotifyUtilities.PlaylistUtilities;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;

import static PlaylistGenerating.PlaylistTypes.CommonUtilities.*;
import static PlaylistGenerating.PlaylistTypes.Interval.IntervalCheckingUtilities.checkIntervalDuration;
import static SpotifyUtilities.PlaylistUtilities.createPlaylist;
import static SpotifyUtilities.UserProfileUtilities.getCurrentUsersProfile;

/**
 * This class is used to generate a playlist for an interval style workout
 */
public class GenerateIntervalOne extends GeneratePlaylist {

    private final int num_intervals;
    protected static int num_tracks = 0;
    protected static int target_length_ms = 0;
    protected static int min_target_length_ms;
    protected static int max_target_length_ms;

    protected static int interval_length_ms = 0;
    protected static int min_interval_length_ms;
    protected static int max_interval_length_ms;

    protected static int tracks_per_interval = 0;
    protected static int num_cool_intervals = 0;
    protected static int num_warm_intervals = 0;
    protected static int og_offset = 3;
    protected enum DURATION_RESULT {
        ACCEPTABLE, TOO_SHORT, TOO_LONG, WITHIN_THIRTY_SECONDS_SHORT, WITHIN_THIRTY_SECONDS_LONG
    }

    protected enum INTERVAL_TYPE {
        SLOW_INTERVAL, FAST_INTERVAL
    }

    /**
     * Constructor for generating a classic style playlist
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @param genres Desired Genres
     * @param age Age of the user
     * @param workout_length Length of the workout
     */
    public GenerateIntervalOne(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity)
            throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException, GetCurrentUsersProfileException {

        super(spotify_api, genres, age, workout_length, intensity);

        num_intervals = getNumIntervals();
        num_tracks = Math.round(workout_length_min / avg_song_len);

        target_length_ms = workout_len_ms;
        setTargetLengths(margin_of_error);

        // unsure if necessary?
        tracks_per_interval = num_tracks / num_intervals;
        num_cool_intervals = (num_intervals - 1) / 2 + 1;
        num_warm_intervals = (num_intervals - 1) / 2;

        //Todo: is this the right way to divide?
        //Todo: should we keep using the same margin  of error for intervals?
        interval_length_ms = target_length_ms / num_intervals;
        setIntervalLengths(margin_of_error);
    }

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
     * Sets the min and max Interval lengths based on the provided margin of error
     *
     * @param margin_of_error margin of error used in setting the min and max interval lengths
     */
    private void setIntervalLengths(float margin_of_error) {
        // Milliseconds of length that is acceptable for the target sequence based on our MOE
        min_interval_length_ms = interval_length_ms - (int) (interval_length_ms * margin_of_error);
        max_interval_length_ms = interval_length_ms + (int) (interval_length_ms * margin_of_error);
    }

    /**
     * Determine the number of intervals we need based on the length of the workout
     * @return number of intervals we need
     */
    private int getNumIntervals() {
        // Todo: test & adjust this function
        // currently truncating anything after the decimal point... should we round up?

        if (workout_length_min <= 30) {
            // if workout is less than 30 minutes, do 1-song intervals
            return (int) ((workout_length_min - avg_song_len) / avg_song_len);
        } else if (workout_length_min <= 60) {
            // if workout is less than 60 minutes, do 2-song intervals
            return (int) ((workout_length_min - (2 * avg_song_len)) / (2*avg_song_len));
        } else if (workout_length_min <= 120) {
            // if workout is more than 60 minutes, do 3-song intervals
            return (int) ((workout_length_min - (3 * avg_song_len)) / (3*avg_song_len));
        } else {
            // if workout is more than 100 minutes, do 4-song intervals
            return (int) ((workout_length_min - (4 * avg_song_len)) / (4*avg_song_len));
        }
    }

    @Override
    public String generatePlaylist() throws Exception {

        User user = getCurrentUsersProfile(spotify_api);

        // Build playlist (get & organize tracks)
        TrackSimplified[] final_playlist_tracks = buildPlaylist();

        eliminateDupesAndNonPlayable(spotify_api, final_playlist_tracks, genres,
                seed_artists, seed_tracks, user.getCountry());

        // Create a playlist on the user's account
        Playlist playlist = createPlaylist(spotify_api, user.getId(), user.getDisplayName());
        String playlist_id = playlist.getId();
        String[] playlist_track_uris = getTrackURIs(final_playlist_tracks);

        System.out.println("Creating Interval One Playlist");
        PlaylistUtilities.addItemsToPlaylist(spotify_api, playlist_id, playlist_track_uris);

        return playlist_id;
    }

    /**
     * Finds tracks for a given interval of the playlist
     * Gets twice the expected number of tracks for a given interval
     * @param interval_type slow_interval or fast_interval
     * @return array of track IDs
     */
    private TrackSimplified[] findTracks(INTERVAL_TYPE interval_type) throws GetRecommendationsException {

        //int local_offset = og_offset; //currently 3
        int limit = 30 * num_intervals; // Is 30 per interval enough?

        do {

            TrackSimplified[] recommended_tracks;

            if (interval_type == INTERVAL_TYPE.SLOW_INTERVAL) {
                recommended_tracks = getSortedRecommendations(limit,
                        resting_bpm - og_offset, resting_bpm + og_offset, resting_bpm);
            } else {
                recommended_tracks = getSortedRecommendations(limit,
                        target_bpm - og_offset, target_bpm + og_offset, target_bpm);
            }

            if (recommended_tracks == null || recommended_tracks.length < limit){
                //System.out.println("null");
                //System.out.println(local_offset);

                og_offset++;
                continue;
            }

            return recommended_tracks;

        } while (true);
    }

    /**
     * Gets tracks and builds a playlist with the proper
     * varying tempo and intervals
     *
     * @return the final array of track IDS
     */
    private TrackSimplified[] buildPlaylist() throws GetRecommendationsException, GetAudioFeaturesForTrackException {

        // Make an ArrayList to hold the final playlist
        ArrayList<TrackSimplified> final_playlist = new ArrayList<>();
        TrackSimplified[] slow_tracks;
        TrackSimplified[] fast_tracks;
        TrackSimplified[] best_fit_tracks;

        boolean isSlowInterval = true;
        float local_moe; // Keeps track of moe for duration purposes which we will be altering here

        // For each interval, get tracks and add them to the playlist
        for (int i = 0; i < num_intervals; i++) {

            local_moe = margin_of_error;
            //int count = 0;

            do {

                // Get new tracks
                slow_tracks = findTracks(INTERVAL_TYPE.SLOW_INTERVAL);
                fast_tracks = findTracks(INTERVAL_TYPE.FAST_INTERVAL);
                // Find a good ordering
                best_fit_tracks = findBestIntervalTracks(slow_tracks, fast_tracks, tracks_per_interval);

                //count++;
                setIntervalLengths(local_moe += .01); // loosen MOE

            } while (best_fit_tracks == null);

            setIntervalLengths(margin_of_error); // restore moe
            isSlowInterval = !isSlowInterval; // Swap flag value

            addAll(final_playlist, best_fit_tracks); // add best fit tracks to final_playlist
        }
        return final_playlist.toArray(TrackSimplified[]::new);
    }

    /**
     * Finds the best sequence of songs that fit within the target duration window
     * Grabs the first batch of songs from the beginning of the tracks array and shifts the selected group
     * of songs to the right until a song is found, or we reach a point where it is clear no grouping is acceptable
     *
     * @param slow_tracks TrackSimplified array to search for a good ordering of target songs in the slow interval
     * @param fast_tracks TrackSimplified array to search for a good ordering of target songs in the fast interval
     * @param num_songs number of songs we want to gather and check against the desired duration
     * @return TrackSimplified array of songs that fit in the target duration window, null otherwise
     */
    private TrackSimplified[] findBestIntervalTracks(TrackSimplified[] slow_tracks,
                                                     TrackSimplified[] fast_tracks, int num_songs) {

        Deque<TrackSimplified> slow_deque = new ArrayDeque<>();
        Deque<TrackSimplified> fast_deque = new ArrayDeque<>();
        HashMap<TrackSimplified, Integer> selected_songs = new HashMap<>();
        int index = 0;
        GenerateIntervalOne.DURATION_RESULT result;
        TrackSimplified current_slow_track;
        TrackSimplified current_fast_track;

        for(int interval = 0; interval < num_intervals; interval++) {



        }

        // Get the first batch of songs into the slow deque
        for (int i = 0; i < num_songs; i++) {

            current_slow_track = slow_tracks[index];

            //
            if(selected_songs.get(current_slow_track) != null){

            }

            slow_deque.add(current_slow_track);
        }

        // Get the first batch of songs into the slow deque
        for (int i = 0; i < num_songs; i++) {

            current_fast_track = fast_tracks[index];

            fast_deque.add(current_fast_track);
        }

        result = checkIntervalDuration(deque);

        if (result == GenerateIntervalOne.DURATION_RESULT.ACCEPTABLE) return deque.toArray(TrackSimplified[]::new);
        // If the shortest combination of tracks is too long there is no suitable combination so return null
        if (result == GenerateIntervalOne.DURATION_RESULT.TOO_LONG) return null;

        for (; index < tracks.length; index++) {

            current_track = tracks[index];

            // This essentially shifts the group of tracks we are analyzing to the right (longer) side
            deque.removeFirst(); // remove the shortest track
            deque.add(current_track); // add the next track in line

            result = checkIntervalDuration(deque);

            if (result == GenerateIntervalOne.DURATION_RESULT.ACCEPTABLE) return deque.toArray(TrackSimplified[]::new);

            if (result == GenerateIntervalOne.DURATION_RESULT.TOO_LONG) return null; // If now too long there is no acceptable combination
        }

        return null;
    }
}