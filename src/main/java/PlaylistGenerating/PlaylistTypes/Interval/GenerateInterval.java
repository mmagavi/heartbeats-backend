package PlaylistGenerating.PlaylistTypes.Interval;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import PlaylistGenerating.PlaylistTypes.GeneratePlaylist;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.*;

import static PlaylistGenerating.PlaylistTypes.CommonUtilities.addAll;
import static SpotifyUtilities.TrackUtilities.duration_comparator;

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

    protected static int query_limit;

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

        if (num_intervals % 2 == 0) num_intervals++; // Ensure we have an odd number of intervals

        num_slow_intervals = (num_intervals / 2) + 1; // We want to start and end with a slow interval
        num_fast_intervals = num_intervals - num_slow_intervals; // Will always be odd if workout len >= 21

        tracks_per_interval = getTracksPerInterval();
        num_slow_tracks = num_slow_intervals * tracks_per_interval;
        num_fast_tracks = num_fast_intervals * tracks_per_interval;

        num_tracks = num_slow_tracks + num_fast_tracks;


        target_length_ms = workout_len_ms;
        setTargetLengths(margin_of_error);

        interval_len_ms = target_length_ms / num_intervals;
        setIntervalLengths(margin_of_error);

        num_levels = num_fast_intervals / 2 + 1;

        //TODO: Make sure this is the ideal calculation, we might want a different ratio
        float length_percentage = workout_length_min / 180f;
        query_limit = Math.round(length_percentage * 100);
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
            // if workout is less than 30 minutes, ..
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
     *
     * @return number of tracks per interval
     */
    private int getTracksPerInterval() {
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
        HashSet<TrackSimplified> track_set = new HashSet<>();

        do {

            recommended_tracks = getUnsortedRecommendations(limit, min_bpm - local_offset,
                    max_bpm + local_offset, target_bpm);


            // Hash set can take the null element which we want to avoid
            if (recommended_tracks != null) {
                track_set.addAll(List.of(recommended_tracks));

                // If the limit has been met
                if (track_set.size() >= limit) {

                    System.out.println("Track Set Size: " + track_set.size());


                    TrackSimplified[] track_array = track_set.toArray(TrackSimplified[]::new);
                    Arrays.sort(track_array, duration_comparator); // Sort the tracks in ascending duration

                    return track_array;
                }
            }

            local_offset++; // Loosen bpm/tempo restrictions

        } while (true);

    }

    /**
     * Gets tracks for a single interval
     *
     * @param tracks - tracks to be used for the interval
     * @return - tracks for the interval
     */
    protected TrackSimplified[] getIntervalTracks(TrackSimplified[] tracks) {

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

    /**
     * Finds tracks for a given bpm, returns the provided limit number of tracks
     *
     * @param query_bpm BPM to query the Spotify recommendations endpoint with
     * @param limit     how many tracks to return (MAX 100)
     * @return arrayList of track IDs
     */
    protected ArrayList<TrackSimplified> getRecommendedTracks(int query_bpm, int limit)
            throws GetRecommendationsException {

        int local_offset = og_offset; // Was og_offset (3) for a while
        // 100 is the max for recommendation endpoint
        HashSet<TrackSimplified> track_set = new HashSet<>();

        do {

            TrackSimplified[] recommended_tracks, recommended_genre_tracks;
            //If this is a personalized playlist getUnsortedRecommendations will pull seed values from the users account
            //Otherwise we will not do this and only query the recommendations endpoint with genre seed values

            if (is_personalized) {
                // We use the unsorted version as they will all be thrown in a hashset anyway, so we will sort later on
                recommended_tracks = getUnsortedRecommendations(limit,
                        query_bpm - local_offset, query_bpm + local_offset, query_bpm);

                // Hash set can take the null element which we want to avoid, also want to avoid adding process if empty
                if (recommended_tracks != null && recommended_tracks.length != 0) {
                    track_set.addAll(List.of(recommended_tracks));
                }

            }

            // We will always pull some by the provided genre
            recommended_genre_tracks = getUnsortedGenreRecommendations(limit,
                    query_bpm - local_offset, query_bpm + local_offset, query_bpm);

            if (recommended_genre_tracks != null && recommended_genre_tracks.length != 0) {
                track_set.addAll(List.of(recommended_genre_tracks));
            }

            // If the limit has been met
            if (track_set.size() >= limit) {
                break;
            }

            local_offset++; // Loosen bpm/tempo restrictions
            System.out.println(local_offset);

        } while (true);

        System.out.println("Track Set Size: " + track_set.size());


        TrackSimplified[] track_array = track_set.toArray(TrackSimplified[]::new);
        Arrays.sort(track_array, duration_comparator); // Sort the tracks in ascending duration

        return new ArrayList<>(Arrays.asList(track_array));

    }

    /**
     * Finds the best sequence of songs that fit within the target duration window
     * Grabs the first batch of songs from the beginning of the tracks array and shifts the selected group
     * of songs to the right until a song is found, or we reach a point where it is clear no grouping is acceptable
     *
     * @return TrackSimplified array of songs that fit in the target duration window, null otherwise
     */
    protected ArrayList<TrackSimplified> findRoughIntervals(ArrayList<TrackSimplified> track_pool, int intervals_to_fill) {

        // track_pool should have 100 song
        Deque<TrackSimplified> deque = new ArrayDeque<>();
        ArrayList<TrackSimplified> selected_tracks = new ArrayList<>();
        TrackSimplified current_track;
        DURATION_RESULT result;
        float acceptable_percent_filled = .75f;

        int index = 0;

        // Get the first batch of songs into the deque
        for (; index < tracks_per_interval; index++) {

            current_track = track_pool.get(index);

            deque.add(current_track);
        }

        int intervals_filled = 0;

        // Pick up where we left off
        for (; index < track_pool.size(); index++) {

            result = checkIntervalDuration(deque);

            if (result == DURATION_RESULT.ACCEPTABLE) {

                int deque_size = deque.size(); // Store size here as in the loop size, will be altered by .pop()

                // Empty the deque, add tracks to the selected tracks, remove songs from track pool
                for (int i = 0; i < deque_size; i++) {
                    current_track = deque.pop();

                    selected_tracks.add(current_track); // Place it in our collection of selected tracks
                    track_pool.remove(current_track); // Removing to avoid having these added later as dupes
                }

                intervals_filled++;

                // If we have filled all intervals break out of this loop
                if (intervals_filled == intervals_to_fill) break;

                index = 0; // restart index at 0 since we altered the track pool and can consider new combinations

                // Refill the deque
                for (; index < tracks_per_interval; index++) {
                    current_track = track_pool.get(index);
                    deque.add(current_track);
                }

            }
            // If the combination is too long there are no combinations remaining that will fit into our intervals
            else if (result == DURATION_RESULT.TOO_LONG) {
                break;
            } else {

                // If duration was not acceptable shift tracks over to find better length
                current_track = track_pool.get(index);

                // This essentially shifts the group of tracks we are analyzing to the right (longer) side
                deque.removeFirst(); // remove the shortest track
                deque.add(current_track); // add the next track in line
            }
        }

        // If we did not fill enough of the intervals return null, so we can try again with looser margins
        float percent_filled = ((float) intervals_filled / intervals_to_fill) * 100;
        System.out.println("Percent Filled: " + percent_filled + "%");

        if (percent_filled < acceptable_percent_filled) return null;

        return selected_tracks;
    }

    /**
     * Takes the given tracks ArrayList and adds the correct number of tracks to it
     *
     * @param tracks              ArrayList of tracks to add to
     * @param query_bpm           bpm to find tracks to fill the intervals with
     * @param total_tracks_needed TOTAL number of tracks needed for the ENTIRE interval range
     *                            (often num_fast_intervals or num_slow_intervals)
     * @return ArrayList of tracks with the correct number of tracks added
     * @throws GetRecommendationsException if there is an error getting recommendations
     */
    protected ArrayList<TrackSimplified> fillIntervals(ArrayList<TrackSimplified> tracks, int total_tracks_needed,
                                                       int query_bpm) throws GetRecommendationsException {

        TrackSimplified[] recommended_tracks;
        TrackSimplified[] tracks_to_add;
        int num_tracks_needed;

        float local_moe; // Keeps track of moe for duration purposes which we will be altering here
        int limit = 21;

        // Find how many tracks we need to find
        num_tracks_needed = total_tracks_needed - tracks.size();

        System.out.println("Tracks Needed: " + num_tracks_needed);

        for (int i = 0; i < num_tracks_needed; i++) {

            local_moe = margin_of_error;

            do {

                recommended_tracks = getRecommendedTracks(limit, query_bpm - og_offset,
                        query_bpm + og_offset, query_bpm);

                tracks_to_add = getIntervalTracks(recommended_tracks);

                //TODO: find good increase to local_moe
                setIntervalLengths(local_moe += .005);

            } while (tracks_to_add == null);

            setIntervalLengths(margin_of_error); // restore moe
            tracks = addAll(tracks, tracks_to_add); // add the new tracks to the track list

        }

        return tracks;
    }

    /**
     * Orders the slow and fast tracks into the correct ordering desired for the final playlist
     *
     * @param slow_tracks tracks in the slow interval
     * @param fast_tracks tracks in the fast interval
     * @return ordered playlist
     */
    protected TrackSimplified[] orderTracks(ArrayList<TrackSimplified> slow_tracks,
                                            ArrayList<TrackSimplified> fast_tracks) {

        TrackSimplified[] ordered_playlist = new TrackSimplified[num_tracks];
        ArrayList<TrackSimplified> current_tracks = slow_tracks;
        boolean is_slow_interval = true; // Every playlist will start with a slow interval

        for (int index = 0; index < num_tracks; index++) {

            // If we have added enough tracks for the current interval, ignore first check when index is 0
            if (index % tracks_per_interval == 0 && index != 0) {

                if (is_slow_interval) {
                    current_tracks = fast_tracks;

                } else {
                    current_tracks = slow_tracks;
                }

                is_slow_interval = !is_slow_interval;
            }

            // Removing index zero will slide all elements to the left refilling index zero until there are no elements
            ordered_playlist[index] = current_tracks.remove(0);

        }

        return ordered_playlist;
    }
}