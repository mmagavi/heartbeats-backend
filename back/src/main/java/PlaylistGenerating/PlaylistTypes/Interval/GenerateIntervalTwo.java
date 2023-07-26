package PlaylistGenerating.PlaylistTypes.Interval;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import ExceptionClasses.TrackExceptions.GetAudioFeaturesForTrackException;
import SpotifyUtilities.PlaylistUtilities;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.*;

import static PlaylistGenerating.PlaylistTypes.CommonUtilities.*;
import static SpotifyUtilities.PlaylistUtilities.createPlaylist;
import static SpotifyUtilities.UserProfileUtilities.getCurrentUsersProfile;

public class GenerateIntervalTwo extends GenerateInterval {

    /**
     * Constructor for generating an Interval style playlist
     * Inherits from the superclass
     *
     * @param spotify_api    SpotifyApi object that has been built with the current user's access token
     * @param genres         Desired Genres
     * @param age            Age of the user
     * @param workout_length Length of the workout
     * @param intensity     Intensity of the workout
     */
    public GenerateIntervalTwo(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity) throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException, GetCurrentUsersProfileException {
        super(spotify_api, genres, age, workout_length, intensity);
    }

    @Override
    public String generatePlaylist() throws Exception {

        System.out.println("Generating Interval Two Playlist");

        User user = getCurrentUsersProfile(spotify_api);

        // Build playlist (get & organize tracks)
        TrackSimplified[] final_playlist_tracks = buildPlaylist();

        eliminateDupesAndNonPlayable(spotify_api, final_playlist_tracks, genres,
                seed_artists, seed_tracks, user.getCountry());

        // Create a playlist on the user's account
        Playlist playlist = createPlaylist(spotify_api, user.getId(), user.getDisplayName());
        String playlist_id = playlist.getId();
        String[] playlist_track_uris = getTrackURIs(final_playlist_tracks);

        System.out.println("Creating Interval Two Playlist");
        PlaylistUtilities.addItemsToPlaylist(spotify_api, playlist_id, playlist_track_uris);

        return playlist_id;
    }

    /**
     * Gets tracks and builds a playlist with the proper
     * varying tempo and interval
     *
     * @return the final array of track IDS
     */
    private TrackSimplified[] buildPlaylist() throws GetRecommendationsException, GetAudioFeaturesForTrackException {

        ArrayList<TrackSimplified> recommended_slow_tracks;
        ArrayList<TrackSimplified> slow_intervals;

        ArrayList<TrackSimplified> fast_intervals;

        TrackSimplified[] final_playlist = null;

        // Keeps track of moe for duration purposes which we will be altering here
        float local_moe = margin_of_error;

        do {

            System.out.println("Getting Recommended Tracks");
            // Get recommended tracks for the slow interval
            recommended_slow_tracks = getRecommendedIntervalTracks(INTERVAL_TYPE.SLOW_INTERVAL, 100);
            //recommended_fast_tracks = null;

//            // Get recommended fast tracks for each fast level
//            for (int i = 0; i < num_levels; i++) {
//                // Store in recommended_fast_tracks hashmap
//                recommended_fast_tracks.put(i, getRecommendedTracks(INTERVAL_TYPE.FAST_INTERVAL, i));
//            }

            System.out.println("Finding Rough Intervals");

            // Fill the intervals the best we can with the given 100 tracks
            slow_intervals = findRoughIntervals(recommended_slow_tracks, INTERVAL_TYPE.SLOW_INTERVAL);
            //recommended_fast_tracks = null;

//            for (int i = 0; i < num_levels; i++) {
//                fast_intervals.put(i, findRoughIntervals(recommended_fast_tracks.get(0), INTERVAL_TYPE.FAST_INTERVAL, i));
//            }

            fast_intervals = findFastIntervals();

            // If enough of the intervals have been found, fill the gaps and sort them into one correctly ordered array
            if(slow_intervals != null && fast_intervals != null){

                System.out.println("Filling Intervals");
                // Find a good ordering of each interval
                slow_intervals = fillIntervals(slow_intervals, INTERVAL_TYPE.SLOW_INTERVAL);

//                // Get tracks for each ArrayList in the fast_intervals hashmap
//                for (int i = 0; i < num_levels; i++) {
//                    fast_intervals.put(i, fillIntervals(fast_intervals.get(0), INTERVAL_TYPE.FAST_INTERVAL, i));
//                }

                System.out.println("Ordering Tracks");

                final_playlist = orderTracks(slow_intervals, fast_intervals);
            }

            setIntervalLengths(local_moe += .01); // loosen MOE

        } while (final_playlist == null);

        setIntervalLengths(margin_of_error); // restore moe

        return final_playlist;
    }



    private ArrayList<TrackSimplified> findFastIntervals() throws GetRecommendationsException{

        // Everything under 21 min has only 5 intervals leaving 3 slow 2 fast in this case there is no 1 peak interval
        // If the playlist is < 21 minutes we need to fill the 2 fast intervals much like the slow intervals
        if(num_fast_intervals == 2){
            return findFastIntervalsShortPlaylist();
        }

        int[] interval_BPMs = getIncreasingIntervalBPMS(); //All the increasing interval's BPMs (including the target)

        TrackSimplified[] recommended_tracks;
        int limit = 60;
        int current_target_bpm;

        for(int interval = 0; interval < interval_BPMs.length; interval++){

            current_target_bpm = interval_BPMs[interval];

            recommended_tracks = getRecommendedTracks(limit, current_target_bpm - 2,
                    current_target_bpm + 2, current_target_bpm);


            //TODO: For each iteration in this loop we are gathering 60 tracks at a tempo level that 2 intervals have
            // (we are going to do the peak interval after this loop). So we need to take this large collections of tracks
            // and do deque stuff to find a good ordering for BOTH intervals. Might have to adapt some of the other deque
            // methods to work with this. Could overload one of those methods so I can pass in number of intervals to fill.
        }


        return new ArrayList<>();
    }

    /**
     * Finds the target BPM associated with each increasing fast interval (including peak) in the playlist
     * and returns those values in a int array. Only increasing intervals are needed as the decreasing intervals have
     * the same BPM targets
     *
     * @return int array of the BPMs associated with each fast interval
     */
    private int[] getIncreasingIntervalBPMS(){

        // In the case playlist length is under 21 minutes, there is only 2 fast intervals of the same BPM
        if(num_fast_intervals == 2){
            return new int[] {target_bpm, target_bpm};
        }

        int[] fast_interval_BPMs = new int[num_fast_intervals]; // We will return this

        // * Example (7 fast intervals), resting 69, target 120 *
        int resting_target_difference = target_bpm - resting_bpm; // * 51 *
        int num_increasing_intervals = Math.round(num_fast_intervals / 2f); // * 4 * round to include peak
        int interval_difference = resting_target_difference / num_increasing_intervals; // * 12.75 -> 12 *


        // So here we will store all the increasing interval's (including the peak interval) corresponding target BPM
        int current_bpm = resting_bpm;

        for(int interval = 0; interval < num_increasing_intervals; interval++){

            current_bpm += interval_difference;

            fast_interval_BPMs[interval] = current_bpm;
        }

        return fast_interval_BPMs;
    }

    /**Only 2 fast intervals
     *
     *
     * @return
     */
    private ArrayList<TrackSimplified> findFastIntervalsShortPlaylist() throws GetRecommendationsException{
        ArrayList<TrackSimplified> recommended_fast_tracks;
        ArrayList<TrackSimplified> fast_intervals;

        float local_moe = margin_of_error;

        do{

            recommended_fast_tracks = getRecommendedIntervalTracks(INTERVAL_TYPE.FAST_INTERVAL, 50);
            fast_intervals = findRoughIntervals(recommended_fast_tracks, INTERVAL_TYPE.FAST_INTERVAL);

            if(fast_intervals != null){

                // MOE is restores in function below, no need to do it here as well

               return fillIntervals(fast_intervals, INTERVAL_TYPE.FAST_INTERVAL);
            }

            setIntervalLengths(local_moe += .01); // loosen MOE

        }while(true);
    }



//    /**
//     * Finds tracks for a given interval of the playlist
//     * Gets twice the expected number of tracks for a given interval
//     *
//     * @param interval_type slow_interval or fast_interval
//     * @return arrayList of track IDs
//     */
//    ArrayList<TrackSimplified> getRecommendedTracks(INTERVAL_TYPE interval_type, int level) throws GetRecommendationsException {
//
//        //int local_offset = og_offset; //currently 3
//        int limit = 100; // 100 is the max for recommendation endpoint
//        int local_offset = og_offset;
//
//        do {
//
//            TrackSimplified[] recommended_tracks;
//
//            if (interval_type == INTERVAL_TYPE.SLOW_INTERVAL) {
//                recommended_tracks = getSortedRecommendations(limit,
//                        resting_bpm - local_offset, resting_bpm + local_offset, resting_bpm);
//            } else {
//
//                int query_bpm = getIntervalBPM(level);
//
//                recommended_tracks = getSortedRecommendations(limit,
//                        query_bpm - local_offset, query_bpm + local_offset, query_bpm);
//            }
//
//            if (recommended_tracks == null || recommended_tracks.length < 90) {
//                local_offset++;
//                continue;
//            }
//
//            System.out.println("Recommended Size: " + recommended_tracks.length);
//            return new ArrayList<>(Arrays.asList(recommended_tracks));
//
//        } while (true);
//    }

//    /**
//     * Finds the best sequence of songs that fit within the target duration window
//     * Grabs the first batch of songs from the beginning of the tracks array and shifts the selected group
//     * of songs to the right until a song is found, or we reach a point where it is clear no grouping is acceptable
//     *
//     * @return TrackSimplified array of songs that fit in the target duration window, null otherwise
//     */
//    ArrayList<TrackSimplified> findRoughIntervals(ArrayList<TrackSimplified> track_pool, INTERVAL_TYPE interval_type, int level)
//            throws GetRecommendationsException {
//
//        // track_pool should have 100 song
//        Deque<TrackSimplified> deque = new ArrayDeque<>();
//        ArrayList<TrackSimplified> selected_tracks = new ArrayList<>();
//        TrackSimplified current_track;
//        DURATION_RESULT result;
//        float acceptable_percent_filled = .75f;
//
//        int index = 0;
//
//        // Get the first batch of songs into the deque
//        for (; index < tracks_per_interval; index++) {
//
//            current_track = track_pool.get(index);
//
//            deque.add(current_track);
//        }
//
//        int intervals_filled = 0;
//        int intervals_to_fill;
//
//        if(interval_type == INTERVAL_TYPE.SLOW_INTERVAL){
//            intervals_to_fill = num_slow_intervals;
//        } // else if we are dealing with fast intervals...
//        else{
//
//
//            // If we are at the highest level and we have an uneven number of fast intervals,
//            // we only need to fill one interval. Else we need to fill two.
//            if (level == num_levels && (num_fast_intervals % 2) == 1) {
//                intervals_to_fill = 1;
//            } else {
//                intervals_to_fill = 2;
//            }
//        }
//
//        // Pick up where we left off
//        for (; index < track_pool.size() ; index++) {
//
//            result = checkIntervalDuration(deque);
//
//            if(result == DURATION_RESULT.ACCEPTABLE){
//
//                int deque_size = deque.size(); // Store size here as in the loop size will be altered by .pop()
//
//                // Empty the deque, add tracks to the selected tracks, remove songs from track pool
//                for(int i = 0; i < deque_size; i++){
//                    current_track = deque.pop();
//
//                    selected_tracks.add(current_track); // Place it in our collection of selected tracks
//                    track_pool.remove(current_track); // Removing to avoid having these added later as dupes
//                }
//
//                intervals_filled++;
//
//                // If we have filled all intervals break out of this loop
//                if(intervals_filled == intervals_to_fill) break;
//
//                index = 0; // restart index at 0 since we altered the track pool and can consider new combinations
//
//                // Refill the deque
//                for(; index < tracks_per_interval; index++){
//                    current_track = track_pool.get(index);
//                    deque.add(current_track);
//                }
//
//            }
//            // If the combination is too long there are no combinations remaining that will fit into our intervals
//            else if (result == DURATION_RESULT.TOO_LONG) {
//                break;
//            }
//            else {
//
//                // If duration was not acceptable shift tracks over to find better length
//                current_track = track_pool.get(index);
//
//                // This essentially shifts the group of tracks we are analyzing to the right (longer) side
//                deque.removeFirst(); // remove the shortest track
//                deque.add(current_track); // add the next track in line
//            }
//        }
//
//        // If we did not fill enough of the intervals return null, so we can try again with looser margins
//        float percent_filled = ((float)intervals_filled / intervals_to_fill) * 100;
//        System.out.println("Percent Filled: " + percent_filled + "%");
//
//        if( percent_filled < acceptable_percent_filled) return null;
//
//        return selected_tracks;
//    }
//
//    /**
//     * Takes the given tracks ArrayList and adds the correct number of tracks to it
//     *
//     * @param tracks - tracks to add to the arrayList
//     * @param interval_type - type of interval we are currently in
//     * @return - ArrayList of tracks with the correct number of tracks added
//     * @throws GetRecommendationsException if there is an error getting recommendations
//     */
//    ArrayList<TrackSimplified> fillIntervals(ArrayList<TrackSimplified> tracks,
//                                             INTERVAL_TYPE interval_type, Integer level) throws GetRecommendationsException {
//
//        TrackSimplified[] recommended_tracks;
//        TrackSimplified[] tracks_to_add = null;
//        int num_tracks_needed;
//        int query_bpm;
//
//        float local_moe; // Keeps track of moe for duration purposes which we will be altering here
//        int limit = 21;
//
//        // Find how many tracks we need to find and at what bpm we should find tracks
//        if(interval_type == INTERVAL_TYPE.SLOW_INTERVAL){
//            num_tracks_needed = num_slow_tracks - tracks.size();
//            query_bpm = resting_bpm;
//        }else{
//            // todo: what is going on here?
//            if (level == num_levels && (num_fast_intervals % 2) == 1) {
//                num_tracks_needed = tracks_per_interval;
//            } else {
//                num_tracks_needed = tracks_per_interval * 2;
//            }
//            query_bpm = getIntervalBPM(level);
//        }
//
//        System.out.println("Tracks Needed: " + num_tracks_needed);
//
//        for(int i = 0; i < num_tracks_needed; i++){
//
//            local_moe = margin_of_error;
//
//            do {
//
//                recommended_tracks = getRecommendedTracks(limit, query_bpm - og_offset,
//                        query_bpm + og_offset, query_bpm);
//
//                tracks_to_add = getIntervalTracks(recommended_tracks);
//
//                setIntervalLengths(local_moe += .005);
//
//            } while (tracks_to_add == null);
//
//            setIntervalLengths(margin_of_error); // restore moe
//            tracks = addAll(tracks, tracks_to_add); // add the new tracks to the track list
//
//        }
//
//        return tracks;
//    }

//
//    /**
//     * Gets the target BPM for that fast interval
//     * Not intended for slow intervals
//     * @return - target BPM
//     */
//    int getIntervalBPM(int level) {
//        int range = target_bpm - resting_bpm;
//        int interval = range / num_levels;
//
//        return resting_bpm + (interval * level);
//    }
}