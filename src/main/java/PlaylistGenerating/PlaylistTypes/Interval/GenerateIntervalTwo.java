package PlaylistGenerating.PlaylistTypes.Interval;

import ExceptionClasses.ArtistExceptions.GetSeveralArtistsException;
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
     * @param intensity      Intensity of the workout
     */
    public GenerateIntervalTwo(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity,
                               boolean is_personalized) throws GetUsersTopArtistsRequestException,
            GetUsersTopTracksRequestException, GetCurrentUsersProfileException, GetSeveralArtistsException {
        super(spotify_api, genres, age, workout_length, intensity, is_personalized);
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

            //TODO: consider reducing limit for closer tempo matches
            //System.out.println("Getting Recommended Tracks");
            // Get recommended tracks for the slow interval
            recommended_slow_tracks = getRecommendedTracks(resting_bpm, query_limit, starting_energy);

            //System.out.println("Finding Slow Intervals");
            // Fill the intervals the best we can with the given 100 tracks
            slow_intervals = findRoughIntervals(recommended_slow_tracks, num_slow_intervals);

            //System.out.println("Finding Fast Intervals");
            fast_intervals = findFastIntervals(); // Finding the fast intervals is much different than finding the slow

            // If enough of the intervals have been found, fill the gaps and sort them into one correctly ordered array
            if (slow_intervals != null && fast_intervals != null) {

                //System.out.println("Filling Slow Intervals");
                // Find a good ordering of each interval
                slow_intervals = fillIntervals(slow_intervals, num_slow_tracks, resting_bpm, starting_energy);

                //System.out.println("Ordering Tracks");
                final_playlist = orderTracks(slow_intervals, fast_intervals);
            }

            setIntervalLengths(local_moe += .01); // loosen MOE

        } while (final_playlist == null);

        setIntervalLengths(margin_of_error); // restore moe

        return final_playlist;
    }

    private ArrayList<TrackSimplified> findFastIntervals() throws GetRecommendationsException {

        // Everything under 21 min has only 5 intervals leaving 3 slow 2 fast in this case there is no 1 peak interval
        // If the playlist is < 21 minutes we need to fill the 2 fast intervals much like the slow intervals
        if (num_fast_intervals == 2) {
            return findFastIntervalsShortPlaylist();
        }

        int[] interval_BPMs = getIncreasingIntervalBPMS(); // All the increasing interval's BPMs (including the peak)
        float[] interval_energies = getIncreasingIntervalEnergies();

        ArrayList<TrackSimplified> fast_tracks = new ArrayList<>(); // Place final tracks here
        Stack<ArrayList<TrackSimplified>> decreasing_intervals_stack = new Stack<>();
        int limit = 60;
        float local_moe = margin_of_error;

        // interval_BPMs length is the same as the number of increasing intervals
        for (int interval = 0; interval < interval_BPMs.length; interval++) {

            // Each target BPM refers to two intervals, one before and after the peak (middle) interval
            int current_target_bpm = interval_BPMs[interval];
            float current_target_energy = interval_energies[interval];

            ArrayList<TrackSimplified> double_interval = null; // reset to null each iteration for below while condition

            do {
                ArrayList<TrackSimplified> recommended_tracks = getRecommendedTracks(current_target_bpm, limit, current_target_energy);

                // Each interval has one corresponding interval to fill, hence the 2 intervals to fill in the call below
                ArrayList<TrackSimplified> rough_intervals = findRoughIntervals(recommended_tracks, 2);

                if (rough_intervals != null) {

                    int total_tracks_needed = tracks_per_interval * 2; // Number of TOTAL tracks needed for 2 intervals
                    double_interval = fillIntervals(rough_intervals, total_tracks_needed, current_target_bpm, current_target_energy);
                }

                setIntervalLengths(local_moe += .01); // loosen MOE

            } while (double_interval == null);

            local_moe = margin_of_error;
            setIntervalLengths(margin_of_error); // restore moe

            SplitArrayList splitInterval = createSplitArrayList(double_interval); // Split double_interval in half

            // Add first half to the ArrayList we will return and add the second half to a stack for later
            fast_tracks.addAll(splitInterval.first_half);
            decreasing_intervals_stack.add(splitInterval.second_half);
        }

        // At this point we need to get the peak interval and then add the tracks in our decreasing interval stack
        ArrayList<TrackSimplified> peak_interval = getPeakInterval();
        fast_tracks.addAll(peak_interval);

        // Now pop off all the ArrayLists in the stack and add to fast_tracks (This results in correct ordering)
        while(!decreasing_intervals_stack.empty()){
            ArrayList<TrackSimplified> interval = decreasing_intervals_stack.pop();
            fast_tracks.addAll(interval);
        }

        return fast_tracks;
    }

    private ArrayList<TrackSimplified> getPeakInterval() throws GetRecommendationsException{

        float local_moe = margin_of_error;

        do {
            ArrayList<TrackSimplified> recommended_tracks = getRecommendedTracks(target_bpm, 30, target_energy);

            // Each interval has one corresponding interval to fill, hence the 2 intervals to fill in the call below
            ArrayList<TrackSimplified> rough_intervals = findRoughIntervals(recommended_tracks, 1);

            if (rough_intervals != null) {

                //MOE restored in fillIntervals
                return fillIntervals(rough_intervals, tracks_per_interval, target_bpm, target_energy);
            }

            setIntervalLengths(local_moe += .01); // loosen MOE

        } while (true);
    }

    private static class SplitArrayList{

        private SplitArrayList(ArrayList<TrackSimplified> first_half, ArrayList<TrackSimplified> second_half){
            this.first_half = first_half;
            this.second_half = second_half;
        }

        ArrayList<TrackSimplified> first_half;
        ArrayList<TrackSimplified> second_half;
    }

    /**
     * Splits the provided ArrayList into two halves and returns the results
     *
     * @param tracks ArrayList of the tracks to be split in half
     * @return SplitArrayList object containing the two halves of the provided tracks ArrayList
     */
    private SplitArrayList createSplitArrayList(ArrayList<TrackSimplified> tracks){

        ArrayList<TrackSimplified> first_half = new ArrayList<>();
        ArrayList<TrackSimplified> second_half = new ArrayList<>();

        int num_tracks = tracks.size();

        for(int index = 0; index < num_tracks; index++){

            TrackSimplified current_track = tracks.get(index);

            if(index < (num_tracks / 2)){
                first_half.add(current_track);
            }else {
                second_half.add(current_track);
            }

        }

        return new SplitArrayList(first_half, second_half);
    }

    /**
     * Finds the target BPM associated with each increasing fast interval (including peak) in the playlist
     * and returns those values in a int array. Only increasing intervals are needed as the decreasing intervals have
     * the same BPM targets
     *
     * @return int array of the BPMs associated with each fast interval
     */
    private int[] getIncreasingIntervalBPMS() {

        // TODO: decide if this if statement is necessary as the calling function deals with this issue earlier
        // In the case playlist length is under 21 minutes, there is only 2 fast intervals of the same BPM
        if (num_fast_intervals == 2) {
            return new int[]{target_bpm, target_bpm};
        }

        // * Example (7 fast intervals), resting 69, target 120 *
        float bpm_difference = target_bpm - resting_bpm; // * 51 *
        int num_increasing_intervals = Math.round(num_fast_intervals / 2f); // * 4 * round to include peak interval
        int interval_difference = Math.round(bpm_difference / num_increasing_intervals); // * 12.75 -> 13 *

        int[] fast_interval_BPMs = new int[num_increasing_intervals]; // We will return this

        // So here we will store all the increasing interval's corresponding target BPM (including the peak interval)
        int current_bpm = resting_bpm;

        for (int interval = 0; interval < num_increasing_intervals; interval++) {

            current_bpm += interval_difference;

            fast_interval_BPMs[interval] = current_bpm;
        }

        return fast_interval_BPMs;
    }

    private float[] getIncreasingIntervalEnergies() {

        // TODO: decide if this if statement is necessary as the calling function deals with this issue earlier
        // In the case playlist length is under 21 minutes, there is only 2 fast intervals of the same BPM
        if (num_fast_intervals == 2) {
            return new float[]{target_energy, target_energy};
        }

        float energy_difference = target_energy - starting_energy;
        int num_increasing_intervals = Math.round(num_fast_intervals / 2f);
        int interval_difference = Math.round(energy_difference / num_increasing_intervals);

        float[] fast_interval_energies = new float[num_increasing_intervals]; // We will return this

        float current_energy = starting_energy;

        for (int interval = 0; interval < num_increasing_intervals; interval++) {

            current_energy += interval_difference;

            fast_interval_energies[interval] = current_energy;
        }

        return fast_interval_energies;
    }

    /**
     * Special case function that finds the tracks for the fast intervals in the event there are only 2 fast intervals.
     * This only happens when the playlist length is under 21 minutes. All other times there is an odd number of fast
     * intervals which results in a singular peak interval rather than the 2 in this case.
     *
     * @return ArrayList of the tracks for the special case fast intervals
     */
    private ArrayList<TrackSimplified> findFastIntervalsShortPlaylist() throws GetRecommendationsException {
        ArrayList<TrackSimplified> recommended_fast_tracks;
        ArrayList<TrackSimplified> fast_intervals;

        float local_moe = margin_of_error;

        do {

            recommended_fast_tracks = getRecommendedTracks(target_bpm, 50, target_energy);
            fast_intervals = findRoughIntervals(recommended_fast_tracks, num_fast_intervals);

            if (fast_intervals != null) {

                // MOE is restores in function below, no need to do it here as well

                return fillIntervals(fast_intervals, num_fast_tracks, target_bpm, target_energy);
            }

            setIntervalLengths(local_moe += .01); // loosen MOE

        } while (true);
    }

}