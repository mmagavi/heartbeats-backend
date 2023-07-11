package PlaylistGenerating.PlaylistTypes.Interval;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import PlaylistGenerating.PlaylistTypes.GeneratePlaylist;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.Arrays;

import static PlaylistGenerating.PlaylistTypes.Interval.IntervalCheckingUtilities.checkPlaylistDuration;
import static SpotifyUtilities.UserProfileUtilities.getCurrentUsersProfile;

/**
 * This class is used to generate a playlist for an interval style workout
 *
 * Plan:
 * First, determine the number of intervals we need:
 * 20 minute playlist -> 5-6 intervals?
 * 180 minute playlist -> ~12 intervals?
 * OR ask user how many intervals they want
 * OR set at 10 intervals? >60min
 * OR do by song length?
 *
 * <30 min -> do by num songs we can fit in
 *
 * Next, determine the length of each interval??
 * try combos?
 *
 * Next, pull songs at resting BPM and at target BPM
 * Two groups of songs, one for resting and one for target
 *
 *
 */
public class GenerateIntervalOne extends GeneratePlaylist {

    private final int num_intervals;
    protected static int num_tracks = 0;
    protected static int target_length_ms = 0;
    protected static int min_target_length_ms;
    protected static int max_target_length_ms;

    protected static int tracks_per_interval = 0;
    protected static int num_cool_intervals = 0;
    protected static int num_warm_intervals = 0;

    //TODO: determine ideal og offset
    protected static int og_offset = 5;

    protected enum DURATION_RESULT {
        ACCEPTABLE, TOO_SHORT, TOO_LONG, WITHIN_THIRTY_SECONDS_SHORT, WITHIN_THIRTY_SECONDS_LONG
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

        tracks_per_interval = num_tracks / num_intervals;
        num_cool_intervals = (num_intervals - 1) / 2 + 1;
        num_warm_intervals = (num_intervals - 1) / 2;
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
     * Determine the number of intervals we need based on the length of the workout
     * @return number of intervals we need
     */
    private int getNumIntervals() {
        // TODO: test & adjust this function
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

        // get cool tracks
        TrackSimplified[] cool_tracks = findCoolTracks();
        // get warm tracks
        TrackSimplified[] warm_tracks = findWarmTracks();

        // concat tracks
        // TODO: implement this function
        TrackSimplified[][] playlist_track_uris = narrowTracks(cool_tracks, warm_tracks);
        // TODO: arrange tracks properly
        // TODO: make sure not null
        TrackSimplified[] final_playlist = concatTracks(playlist_track_uris[0], playlist_track_uris[1]);

        // TODO: create playlist & return id

        return null;
    }

    /**
     * Finds tracks for the 'cool' regions of the playlist
     * @return array of track IDs
     */
    private TrackSimplified[] findCoolTracks() throws GetRecommendationsException {

        String[] track_ids = null;

        int local_offset = og_offset; //currently 5

        do {
            // get num_tracks tracks from the resting BPM (twice as many as we need)
            TrackSimplified[] recommended_tracks = getSortedRecommendations(num_tracks,
                    resting_bpm - local_offset, resting_bpm + local_offset, resting_bpm);

            if (recommended_tracks != null) return recommended_tracks;

            System.out.println("null");
            System.out.println(local_offset);

            local_offset++;

        } while (true);
    }

    /**
     * Finds tracks for the 'warm' regions of the playlist
     * @return array of track IDs
     */
    private TrackSimplified[] findWarmTracks() throws GetRecommendationsException {

        int local_offset = og_offset; // currently 5

        do {
            // get num_tracks tracks from the resting BPM (twice as many as we need)
            TrackSimplified[] recommended_tracks = getSortedRecommendations(num_tracks,
                    target_bpm - local_offset, target_bpm + local_offset, target_bpm);

            if (recommended_tracks != null) return recommended_tracks;

            System.out.println("null");
            System.out.println(local_offset);

            local_offset++;

        } while (true);
    }

    /**
     * Narrows down cool and warm tracks so when they are combined they will be the proper length
     * DOES NOT CONCATENATE OR ORGANIZE INTO INTERVALS
     *
     * @param cool_tracks tracks for the cool regions of the playlist
     * @param warm_tracks tracks for the warm regions of the playlist
     *
     * @return ARRAY OF arrays of track IDs - cool tracks and then warm tracks
     */
    private TrackSimplified[][] narrowTracks(TrackSimplified[] cool_tracks, TrackSimplified[] warm_tracks) {

        // Determine the number cool and warm songs we need
        int num_warm_tracks = num_warm_intervals * tracks_per_interval;
        int num_cool_tracks = num_cool_intervals * tracks_per_interval;

        // edge case: try the shortest possible combination of songs
        // TODO: probably not good to make a copy of the array...
        // TODO: ask dalton about using Deques instead of arrays..
        DURATION_RESULT shortest_duration = checkPlaylistDuration(Arrays.copyOfRange(cool_tracks, 0, num_cool_tracks), Arrays.copyOfRange(warm_tracks, 0, num_warm_tracks));

        // if by an off chance this is perfect....
        // TODO: do i need to leave this in?
        if (shortest_duration == DURATION_RESULT.ACCEPTABLE) {
            return new TrackSimplified[][]{Arrays.copyOfRange(cool_tracks, 0, num_cool_tracks), Arrays.copyOfRange(warm_tracks, 0, num_warm_tracks)};
        }

        // while the shortest duration is too short, add a song to the cool tracks
        while (shortest_duration == DURATION_RESULT.TOO_SHORT) {
            num_tracks++;
            tracks_per_interval = num_tracks / num_intervals;
            num_warm_tracks = num_warm_intervals * tracks_per_interval;
            num_cool_tracks = num_cool_intervals * tracks_per_interval;

            shortest_duration = checkPlaylistDuration(Arrays.copyOfRange(cool_tracks, 0, num_cool_tracks), Arrays.copyOfRange(warm_tracks, 0, num_warm_tracks));
        }

        // edge case: try the longest possible combination of songs
        DURATION_RESULT longest_duration = checkPlaylistDuration(Arrays.copyOfRange(cool_tracks, cool_tracks.length - num_cool_tracks - 1,
                cool_tracks.length - 1), Arrays.copyOfRange(warm_tracks, warm_tracks.length - 1 - num_warm_tracks, warm_tracks.length - 1));

        // if by an off chance this is perfect....
        // TODO: do i need to leave this in?
        if (longest_duration == DURATION_RESULT.ACCEPTABLE) {
            return new TrackSimplified[][]{Arrays.copyOfRange(cool_tracks, cool_tracks.length - num_cool_tracks - 1,
                    cool_tracks.length - 1), Arrays.copyOfRange(warm_tracks, warm_tracks.length - 1 - num_warm_tracks, warm_tracks.length - 1)};
        }

        // while duration is too long, remove songs from total num tracks
        while (longest_duration == DURATION_RESULT.TOO_LONG) {
            num_tracks--;
            tracks_per_interval = num_tracks / num_intervals;
            num_warm_tracks = num_warm_intervals * tracks_per_interval;
            num_cool_tracks = num_cool_intervals * tracks_per_interval;

            longest_duration = checkPlaylistDuration(Arrays.copyOfRange(cool_tracks, cool_tracks.length - num_cool_tracks - 1,
                    cool_tracks.length - 1), Arrays.copyOfRange(warm_tracks, warm_tracks.length - 1 - num_warm_tracks, warm_tracks.length - 1));
        }

        // TODO: we can keep checking and upping the starting and ending index until we find a good combination


        return null;
    }

    /**
     * Concatenates the cool and warm tracks into one playlist with the proper
     * varying tempo and intervals
     *
     * @param cool_tracks tracks for the cool regions of the playlist
     * @param warm_tracks tracks for the warm regions of the playlist
     * @return the final array of track IDS
     */
    private TrackSimplified[] concatTracks(TrackSimplified[] cool_tracks, TrackSimplified[] warm_tracks) {

        //TODO: placeholder. should be straightforward


        return null;
    }
}