package PlaylistGenerating.PlaylistTypes.Interval;
import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import PlaylistGenerating.PlaylistTypes.GeneratePlaylist;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.ArrayDeque;
import java.util.Deque;

public class GenerateInterval extends GeneratePlaylist {

    protected static int num_tracks = 0;
    protected static int target_length_ms = 0;
    protected static int min_target_len_ms;
    protected static int max_target_len_ms;
    protected static int interval_len_ms = 0;
    protected static int min_interval_len_ms;
    protected static int max_interval_len_ms;
    protected static int tracks_per_interval = 0;
    protected static int num_slow_intervals = 0;
    protected static int num_fast_intervals = 0;
    protected static int og_offset = 3;
    protected static int num_slow_tracks;
    protected static int num_fast_tracks;
    protected static int num_levels = -1;

    protected enum INTERVAL_TYPE {
        SLOW_INTERVAL, FAST_INTERVAL
    }

    /**
     * Constructor for generating a classic style playlist
     *
     * @param spotify_api    SpotifyApi object that has been built with the current user's access token
     * @param genres         Desired Genres
     * @param age            Age of the user
     * @param workout_length Length of the workout
     */
    public GenerateInterval(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity)
            throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException, GetCurrentUsersProfileException {

        super(spotify_api, genres, age, workout_length, intensity);

        int num_intervals = getNumIntervals();

        if(num_intervals % 2 == 0) num_intervals++; // Ensure we have an odd number of intervals

        num_slow_intervals = (num_intervals / 2) + 1; // We want to start and end with a slow interval
        num_fast_intervals = num_intervals - num_slow_intervals;

        tracks_per_interval = getTracksPerInterval();
        num_slow_tracks = num_slow_intervals * tracks_per_interval;
        num_fast_tracks = num_fast_intervals * tracks_per_interval;

        num_tracks = num_slow_tracks + num_fast_tracks;


        target_length_ms = workout_len_ms;
        setTargetLengths(margin_of_error);

        interval_len_ms = target_length_ms / num_intervals;
        setIntervalLengths(margin_of_error);

        num_levels = num_fast_intervals / 2 + 1;
    }

    @Override
    public String generatePlaylist() throws Exception {
        return null;
    }

    /**
     * Determine the number of intervals we need based on the length of the workout
     *
     * @return number of intervals we need
     */
    private int getNumIntervals() {
        // Todo: test & adjust this function
        // currently truncating anything after the decimal point... should we round up?

        if (workout_length_min <= 30) {
            // if workout is less than 30 minutes, do 1-song intervals
            return (int) (workout_length_min / avg_song_len);
        } else if (workout_length_min <= 60) {
            // if workout is less than 60 minutes, do 2-song intervals
            return (int) (workout_length_min / (2 * avg_song_len));
        } else if (workout_length_min <= 120) {
            // if workout is less than 120 minutes, do 3-song intervals
            return (int) (workout_length_min / (3 * avg_song_len));
        } else {
            // if workout is more than 120 minutes, do 4-song intervals
            return (int) (workout_length_min / (4 * avg_song_len));
        }
    }


    /**
     * Determine the number of tracks per interval based on the length of the workout
     * @return number of tracks per interval
     */
    private int getTracksPerInterval(){
        if (workout_length_min <= 30) {
            // if workout is less than 30 minutes, do 1-song intervals
            return 1;
        } else if (workout_length_min <= 60) {
            // if workout is less than 60 minutes, do 2-song intervals
            return 2;
        } else if (workout_length_min <= 120) {
            // if workout is less than 120 minutes, do 3-song intervals
            return 3;
        } else {
            // if workout is more than 120 minutes, do 4-song intervals
            return 4;
        }
    }

    /**
     * Sets the min and max Target lengths based on the provided margin of error
     *
     * @param margin_of_error margin of error used in setting the min and max target lengths
     */
    private void setTargetLengths(float margin_of_error) {
        // MilliSeconds of length that is acceptable for the target sequence based on our MOE
        min_target_len_ms = target_length_ms - (int) (target_length_ms * margin_of_error);
        max_target_len_ms = target_length_ms + (int) (target_length_ms * margin_of_error);
    }

    /**
     * Sets the min and max Interval lengths based on the provided margin of error
     *
     * @param margin_of_error margin of error used in setting the min and max interval lengths
     */
    void setIntervalLengths(float margin_of_error) {
        // Milliseconds of length that is acceptable for the target sequence based on our MOE
        min_interval_len_ms = interval_len_ms - (int) (interval_len_ms * margin_of_error);
        max_interval_len_ms = interval_len_ms + (int) (interval_len_ms * margin_of_error);
    }

    /**
     * Checks if the track array provided is within the allowable duration range
     * SPECIFICALLY FOR INTERVAL SEQUENCES ONLY
     *
     * @param tracks tracks to be checked for their duration
     * @return appropriately named enum (TOO_SHORT if too short, TOO_LONG if too long, and ACCEPTABLE if acceptable)
     */
    protected static DURATION_RESULT checkIntervalDuration(Deque<TrackSimplified> tracks) {
        int duration_ms = 0;

        for (TrackSimplified track : tracks) {
            duration_ms += track.getDurationMs();
        }

        System.out.println("Duration: " + duration_ms);
        System.out.println("Interval Duration: " + interval_len_ms);
        System.out.println("Min Duration: " + min_interval_len_ms);
        System.out.println("Max Duration: " + max_interval_len_ms);
        System.out.println();


        if (duration_ms < min_interval_len_ms) {
            return DURATION_RESULT.TOO_SHORT;
        } else if (duration_ms > max_interval_len_ms) {
            return DURATION_RESULT.TOO_LONG;
        } else {
            return DURATION_RESULT.ACCEPTABLE;
        }
    }

    /**
     * Queries the Spotify recommendation endpoint with protections to ensure the correct number of tracks are returned
     *
     * @param min_bpm    minimum bpm for recommendation endpoint query
     * @param max_bpm    maximum bpm for recommendation endpoint query
     * @param target_bpm target bpm for recommendation endpoint query
     * @return TrackSimplified array of tracks from the recommendation endpoint
     * @throws GetRecommendationsException If an error was encountered in the recommendation endpoint
     */
    TrackSimplified[] getRecommendedTracks(int limit, int min_bpm, int max_bpm, int target_bpm)
            throws GetRecommendationsException {

        TrackSimplified[] recommended_tracks;
        int local_offset = 0;

        do {

            recommended_tracks = getSortedRecommendations(limit, min_bpm - local_offset,
                    max_bpm + local_offset, target_bpm);

            local_offset++; // increase offset to find more tracks as the current bpm boundaries may be too restrictive

        } while (recommended_tracks == null || recommended_tracks.length < limit);

        return recommended_tracks;
    }

    /**
     * Gets tracks for a single interval
     * @param tracks - tracks to be used for the interval
     * @return - tracks for the interval
     */
    TrackSimplified[] getIntervalTracks(TrackSimplified[] tracks){

        Deque<TrackSimplified> deque = new ArrayDeque<>();

        int index = 0;
        DURATION_RESULT result;
        TrackSimplified current_track;

        // Get the first batch of songs into the deque
        for (; index < tracks_per_interval; index++) {
            current_track = tracks[index];
            deque.add(current_track);
        }

        result = checkIntervalDuration(deque);

        if (result == DURATION_RESULT.ACCEPTABLE) return deque.toArray(TrackSimplified[]::new);
        // If the shortest combination of tracks is too long there is no suitable combination so return null
        if (result == DURATION_RESULT.TOO_LONG) return null;

        for (; index < tracks.length; index++) {

            current_track = tracks[index];

            // This essentially shifts the group of tracks we are analyzing to the right (longer) side
            deque.removeFirst(); // remove the shortest track
            deque.add(current_track); // add the next track in line

            result = checkIntervalDuration(deque);

            if (result == DURATION_RESULT.ACCEPTABLE) return deque.toArray(TrackSimplified[]::new);

            if (result == DURATION_RESULT.TOO_LONG) return null; // If now too long there is no acceptable combination
        }

        return null;
    }
}
