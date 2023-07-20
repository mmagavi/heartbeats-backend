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

import java.util.*;

import static PlaylistGenerating.PlaylistTypes.CommonUtilities.*;
import static SpotifyUtilities.PlaylistUtilities.createPlaylist;
import static SpotifyUtilities.UserProfileUtilities.getCurrentUsersProfile;

/**
 * This class is used to generate a playlist for an interval style workout
 */
public class GenerateIntervalOne extends GenerateInterval {

    /**
     * Constructor for generating an interval one style playlist
     * Inherits from the superclass
     *
     * @param spotify_api    SpotifyApi object that has been built with the current user's access token
     * @param genres         Desired Genres
     * @param age            Age of the user
     * @param workout_length Length of the workout
     * @param intensity    Intensity of the workout
     */
    public GenerateIntervalOne(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity) throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException, GetCurrentUsersProfileException {
        super(spotify_api, genres, age, workout_length, intensity);
    }

    @Override
    public String generatePlaylist() throws Exception {

        System.out.println("Generating Interval One Playlist");

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
     * Gets tracks and builds a playlist with the proper
     * varying tempo and intervals
     *
     * @return the final array of track IDS
     */
    private TrackSimplified[] buildPlaylist() throws GetRecommendationsException, GetAudioFeaturesForTrackException {

        ArrayList<TrackSimplified> recommended_slow_tracks;
        ArrayList<TrackSimplified> slow_intervals;

        ArrayList<TrackSimplified> recommended_fast_tracks;
        ArrayList<TrackSimplified> fast_intervals;

        TrackSimplified[] final_playlist = null;

        // Keeps track of moe for duration purposes which we will be altering here
        float local_moe = margin_of_error;

        do {

            System.out.println("Getting Recommended Tracks");
            // Get recommended tracks (size of 100 each)
            recommended_slow_tracks = getRecommendedTracks(INTERVAL_TYPE.SLOW_INTERVAL);
            recommended_fast_tracks = getRecommendedTracks(INTERVAL_TYPE.FAST_INTERVAL);

            System.out.println("Finding Rough Intervals");
            // Fill the intervals the best we can with the given 100 tracks
            slow_intervals = findRoughIntervals(recommended_slow_tracks, INTERVAL_TYPE.SLOW_INTERVAL);
            fast_intervals = findRoughIntervals(recommended_fast_tracks, INTERVAL_TYPE.FAST_INTERVAL);

            // If enough of the intervals have been found, fill the gaps and sort them into one correctly ordered array
            if(slow_intervals != null && fast_intervals != null){

                System.out.println("Filling Intervals");
                // Find a good ordering of each interval
                slow_intervals = fillIntervals(slow_intervals, INTERVAL_TYPE.SLOW_INTERVAL);
                fast_intervals = fillIntervals(fast_intervals, INTERVAL_TYPE.FAST_INTERVAL);

                System.out.println("Ordering Tracks");
                // Order the tracks correctly
                final_playlist = orderTracks(slow_intervals, fast_intervals);
            }

            setIntervalLengths(local_moe += .01); // loosen MOE

        } while (final_playlist == null);

        setIntervalLengths(margin_of_error); // restore moe

        return final_playlist;
    }

    /**
     * Finds tracks for a given interval of the playlist
     * Gets twice the expected number of tracks for a given interval
     *
     * @param interval_type slow_interval or fast_interval
     * @return arrayList of track IDs
     */
    ArrayList<TrackSimplified> getRecommendedTracks(INTERVAL_TYPE interval_type) throws GetRecommendationsException {

        //int local_offset = og_offset; //currently 3
        int limit = 100; // 100 is the max for recommendation endpoint
        int local_offset = og_offset;

        do {

            TrackSimplified[] recommended_tracks;

            if (interval_type == INTERVAL_TYPE.SLOW_INTERVAL) {
                recommended_tracks = getSortedRecommendations(limit,
                        resting_bpm - local_offset, resting_bpm + local_offset, resting_bpm);
            } else {
                recommended_tracks = getSortedRecommendations(limit,
                        target_bpm - local_offset, target_bpm + local_offset, target_bpm);
            }


            //TODO: look at this 90 and see if another value is better
            if (recommended_tracks == null || recommended_tracks.length < 90) {
                local_offset++;
                continue;
            }

            System.out.println("Recommended Size: " + recommended_tracks.length);
            return new ArrayList<>(Arrays.asList(recommended_tracks));

        } while (true);
    }

    /**
     * Finds the best sequence of songs that fit within the target duration window
     * Grabs the first batch of songs from the beginning of the tracks array and shifts the selected group
     * of songs to the right until a song is found, or we reach a point where it is clear no grouping is acceptable
     *
     * @return TrackSimplified array of songs that fit in the target duration window, null otherwise
     */
    ArrayList<TrackSimplified> findRoughIntervals(ArrayList<TrackSimplified> track_pool, INTERVAL_TYPE interval_type)
            throws GetRecommendationsException {

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
        int intervals_to_fill;

        if(interval_type == INTERVAL_TYPE.SLOW_INTERVAL){
            intervals_to_fill = num_slow_intervals;
        }
        else{
            intervals_to_fill = num_fast_intervals;
        }

        // Pick up where we left off
        for (; index < track_pool.size() ; index++) {

            result = checkIntervalDuration(deque);

            if(result == DURATION_RESULT.ACCEPTABLE){

                int deque_size = deque.size(); // Store size here as in the loop size will be altered by .pop()

                // Empty the deque, add tracks to the selected tracks, remove songs from track pool
                for(int i = 0; i < deque_size; i++){
                    current_track = deque.pop();

                    selected_tracks.add(current_track); // Place it in our collection of selected tracks
                    track_pool.remove(current_track); // Removing to avoid having these added later as dupes
                }

                intervals_filled++;

                // If we have filled all intervals break out of this loop
                if(intervals_filled == intervals_to_fill) break;

                index = 0; // restart index at 0 since we altered the track pool and can consider new combinations

                // Refill the deque
                for(; index < tracks_per_interval; index++){
                    current_track = track_pool.get(index);
                    deque.add(current_track);
                }

            }
            // If the combination is too long there are no combinations remaining that will fit into our intervals
            else if (result == DURATION_RESULT.TOO_LONG) {
                break;
            }
            else {

                // If duration was not acceptable shift tracks over to find better length
                current_track = track_pool.get(index);

                // This essentially shifts the group of tracks we are analyzing to the right (longer) side
                deque.removeFirst(); // remove the shortest track
                deque.add(current_track); // add the next track in line
            }
        }

        // If we did not fill enough of the intervals return null, so we can try again with looser margins
        float percent_filled = ((float)intervals_filled / intervals_to_fill) * 100;
        System.out.println("Percent Filled: " + percent_filled + "%");

        if( percent_filled < acceptable_percent_filled) return null;

        return selected_tracks;
    }

    /**
     * Takes the given tracks ArrayList and adds the correct number of tracks to it
     *
     * @param tracks ArrayList of tracks to add to
     * @param interval_type slow_interval or fast_interval
     * @return ArrayList of tracks with the correct number of tracks added
     * @throws GetRecommendationsException if there is an error getting recommendations
     */
    ArrayList<TrackSimplified> fillIntervals(ArrayList<TrackSimplified> tracks,
                                             INTERVAL_TYPE interval_type) throws GetRecommendationsException {

        TrackSimplified[] recommended_tracks;
        TrackSimplified[] tracks_to_add = null;
        int num_tracks_needed;
        int query_bpm;

        float local_moe; // Keeps track of moe for duration purposes which we will be altering here
        int limit = 21;

        // Find how many tracks we need to find and at what bpm we should find tracks
        if(interval_type == INTERVAL_TYPE.SLOW_INTERVAL){
            num_tracks_needed = num_slow_tracks - tracks.size();
            query_bpm = resting_bpm;
        }else{
            num_tracks_needed = num_fast_tracks - tracks.size();
            query_bpm = target_bpm;
        }

        System.out.println("Tracks Needed: " + num_tracks_needed);

        for(int i = 0; i < num_tracks_needed; i++){

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
     * @param slow_tracks tracks in the slow interval
     * @param fast_tracks tracks in the fast interval
     * @return ordered playlist
     */
    TrackSimplified[] orderTracks(ArrayList<TrackSimplified> slow_tracks,
                                  ArrayList<TrackSimplified> fast_tracks){

        TrackSimplified[] ordered_playlist = new TrackSimplified[num_tracks];
        ArrayList<TrackSimplified> current_tracks = slow_tracks;
        boolean is_slow_interval = true; // Every playlist will start with a slow interval

        for(int index = 0; index < num_tracks; index++){

            // If we have added enough tracks for the current interval, ignore first check when index is 0
            if( index % tracks_per_interval == 0 && index != 0){

                if(is_slow_interval){
                    current_tracks = fast_tracks;

                }else{
                    current_tracks = slow_tracks;
                }

                is_slow_interval = !is_slow_interval;
            }

            ordered_playlist[index] = current_tracks.remove(0);

        }

        return ordered_playlist;
    }
}