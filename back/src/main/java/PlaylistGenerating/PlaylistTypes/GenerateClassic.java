package PlaylistGenerating.PlaylistTypes;

import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import PlaylistGenerating.TargetHeartRateRange;
import SpotifyUtilities.RecommendationArguments;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.Arrays;
import java.util.HashMap;

import static PlaylistGenerating.DesiredHeartRateRanges.getTargetHeartRateRange;
import static SpotifyUtilities.BrowsingUtilities.getRecommendations;
import static SpotifyUtilities.PersonalizationUtilities.GetUsersTopArtists;
import static SpotifyUtilities.PersonalizationUtilities.GetUsersTopTracks;
import static SpotifyUtilities.PlaylistUtilities.createPlaylist;
import static SpotifyUtilities.TrackUtilities.duration_comparator;
import static SpotifyUtilities.UserProfileUtilities.getCurrentUsersProfile;

public class GenerateClassic extends GeneratePlaylist {

    private final float warmup_wind_down_length; // minutes
    private String seed_artists;
    private final int seed_genres_provided;
    private int desired_num_seed_artists;
    private int desired_num_seed_tracks;
    private final int target_bpm;
    private final float bpm_difference;
    private int num_intervals;

    /**
     * Constructor for generating a classic style playlist
     *
     * @param spotify_api    SpotifyApi object that has been built with the current user's access token
     * @param genres         Desired Genres
     * @param age            Age of the user
     * @param workout_length Length of the workout
     * @param intensity      desired intensity (low, medium, high)
     */
    public GenerateClassic(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity) {

        super(spotify_api, genres, age, workout_length, intensity);

        target_bpm = getTargetBPM();

        // warmup and wind-down are the same length and are 10% of the workout each
        warmup_wind_down_length = workout_length * .1f;

        // Only 5 seed values across tracks, artists, and genres can be provided to the recommendations endpoint
        // so if less than the max of 3 genres the front end lets the user choose from were chosen we can provide
        // additional seed artists or seed tracks
        seed_genres_provided = (int) genres.chars().filter(ch -> ch == ',').count();

        determineSeedLimits();

        // Number of intervals in which there is one song per interval (For warmup/wind-down only)
        // We will round up to avoid intervals needing to have exceptionally long songs
        num_intervals = Math.round(warmup_wind_down_length / 3f);

        bpm_difference = findBpmDifference();

    }

    /**
     * Calculate the bpm difference between each interval in the warmup/wind-down sequence
     *
     * @return (float) BPM difference between intervals
     */
    private float findBpmDifference() {
        // Special case for workouts less than 45 minutes, we want one interval with the bpm difference being halfway
        // between the resting bpm and target bpm
        if (num_intervals < 2) {
            num_intervals = 1;
            return (float) ((target_bpm - resting_bpm) / 2);
        } else {
            // By finding the difference between the target bpm and resting bpm and finally dividing by the number of
            // intervals in the warmup we find the tempo difference between each interval
            return (float) ((target_bpm - resting_bpm) / num_intervals);
        }
    }

    @Override
    public String generatePlaylist() throws Exception {

        User user = getCurrentUsersProfile(spotify_api);

        // Create a playlist on the user's account
        Playlist playlist = createPlaylist(spotify_api, user.getId(), user.getDisplayName());

        String playlist_id = playlist.getId();

        String[] warmup_track_ids = getWarmupTracks();

        // PlaylistUtilities.addItemsToPlaylist(spotify_api, playlist_id, warmup_track_ids);

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
     * Gets the tracks for the warmup sequence of the workout
     *
     * @return String Array of track ids for the warmup sequence
     * @throws Exception if one of the API calls encounters an issue, will be tossed up to the calling function
     */
    private String[] getWarmupTracks() throws Exception {

        HashMap<Integer, TrackSimplified[]> sorted_intervals;

        boolean ordering_not_found = true; // flag for while loop
        String[] warmup_track_ids = new String[num_intervals];

        do{
            sorted_intervals = getSortedIntervals(true);



        }while(ordering_not_found); // if a good ordering was not found we will retry

        return warmup_track_ids;

    }

    /**
     * Gets the tracks for the wind down sequence of the workout
     *
     * @return String Array of track ids for the warmup sequence
     * @throws Exception if one of the API calls encounters an issue, will be tossed up to the calling function
     */
    private String[] getWindDownTracks() throws Exception {

        HashMap<Integer, TrackSimplified[]> sorted_intervals = getSortedIntervals(false);

        String[] wind_down_track_ids = new String[num_intervals];

        return wind_down_track_ids;

    }

    /**
     * Fetches songs in each interval of the warmup/wind down sequences and sorts each interval by duration
     *
     * @param is_warmup indicates if the desired sorted intervals to be returned should be for the warmup sequence
     * @return Hashmap of Integer keys representing each interval [0 - num_intervals) and TrackSimplified[] as values
     * @throws Exception if one of the API calls encounters an issue, will be tossed up to the calling function
     */
    private HashMap<Integer, TrackSimplified[]> getSortedIntervals(boolean is_warmup) throws Exception {

        String seed_artists = getSeedArtists();
        String seed_tracks = getSeedTracks();

        HashMap<Integer, TrackSimplified[]> intervals = new HashMap<>();

        //TODO: Play around with limit and see how few songs we can fetch while still getting good results
        int limit = 10; // Number of tracks we want to get

        float query_bpm = initializeQueryBPM(is_warmup); // will be updated in for-loop

//        System.out.println("Resting BPM: " + resting_bpm);
//        System.out.println("Target BPM: " + target_bpm);
//        System.out.println("num_intervals: " + num_intervals);
//        System.out.println("BPM difference: " + bpm_difference);

        int offset = 1; // How far from the query bpm we want song tempos in the recommendations request below

        // We need to call the recommendations endpoint for each interval, fetching a few songs in that interval's range
        // This yields better results than requesting a lot of songs in a large range
        for (int current_interval = 0; current_interval < num_intervals; current_interval++) {

            // Update BPM to the next interval
            query_bpm = updateQueryBPM(query_bpm, is_warmup);

            //System.out.println(query_bpm);

            RecommendationArguments current_arguments = new RecommendationArguments(
                    spotify_api, limit, genres, seed_artists, seed_tracks,
                    query_bpm - offset, query_bpm + offset, query_bpm);

            Recommendations recommendations = getRecommendations(current_arguments);

            TrackSimplified[] recommended_tracks = recommendations.getTracks();

            Arrays.sort(recommended_tracks, duration_comparator);

            intervals.put(current_interval, recommended_tracks);
        }

        return intervals;
    }

    /**
     * Updates the query bpm based on the isWarmup boolean
     *
     * @param query_bpm the bpm to be updated and returned
     * @param is_warmup  if true query bpm will be increased otherwise decreased
     * @return updated query bpm
     */
    private float updateQueryBPM(float query_bpm, boolean is_warmup) {
        if (is_warmup) {
            return query_bpm += bpm_difference; // If warming up we want to increase the BPM
        } else {
            return query_bpm -= bpm_difference; // If winding down we want to decrease the BPM
        }
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
     * Loops through all the given tracks and stores their IDs in a string array which is then returned
     *
     * @param tracks array of tracks to fetch the id from
     * @param limit  number of tracks in the array
     * @return String array of all the given track's ids
     */
    private String[] getTrackIDs(TrackSimplified[] tracks, int limit) {

        String[] ids = new String[limit];

        for (int index = 0; index < limit; index++) {
            ids[index] = tracks[index].getId();
        }

        return ids;
    }
}