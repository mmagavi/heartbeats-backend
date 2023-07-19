package PlaylistGenerating.PlaylistTypes;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import PlaylistGenerating.HeartRateRanges.TargetHeartRateRange;
import SpotifyUtilities.RecommendationArguments;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.Arrays;

import static PlaylistGenerating.HeartRateRanges.DesiredHeartRateRanges.getTargetHeartRateRange;
import static SpotifyUtilities.BrowsingUtilities.getRecommendationTempoRange;
import static SpotifyUtilities.BrowsingUtilities.getRecommendations;
import static SpotifyUtilities.PersonalizationUtilities.GetUsersTopArtists;
import static SpotifyUtilities.PersonalizationUtilities.GetUsersTopTracks;
import static SpotifyUtilities.TrackUtilities.duration_comparator;
import static SpotifyUtilities.UserProfileUtilities.getCurrentUsersProfile;

abstract public class GeneratePlaylist {

    public static enum DURATION_RESULT {
        ACCEPTABLE, TOO_SHORT, TOO_LONG, WITHIN_THIRTY_SECONDS_SHORT, WITHIN_THIRTY_SECONDS_LONG
    }

    protected final String genres;
    // The spotifyAPI containing the user's information
    protected final SpotifyApi spotify_api;
    protected final int age;
    protected final int workout_length_min;
    protected final int workout_len_ms; // Length of the workout in MilliSeconds
    protected final String intensity;
    protected final int resting_bpm = 69; // assuming an average resting bpm
    protected final float margin_of_error; // percent a playlist can be off the target by and still be acceptable
    protected final float avg_song_len = 3.5f;
    protected int seed_genres_provided = 0;
    protected int desired_num_seed_artists;
    protected int desired_num_seed_tracks;
    protected String seed_artists;
    protected String seed_tracks;
    protected final int target_bpm;

    protected User user;

    public GeneratePlaylist(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity)
    throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException, GetCurrentUsersProfileException {

        this.spotify_api = spotify_api;
        this.genres = genres;
        this.age = age;
        this.workout_length_min = workout_length;
        this.workout_len_ms = workout_length * 60_000;
        this.intensity = intensity;
        this.margin_of_error = .02f;
        this.user = getCurrentUsersProfile(spotify_api);

        target_bpm = getTargetBPM();

        // Only 5 seed values across tracks, artists, and genres can be provided to the recommendations endpoint
        // so if less than the max of 3 genres the front end lets the user choose from were chosen we can provide
        // additional seed artists or seed tracks
        seed_genres_provided = (int) genres.chars().filter(ch -> ch == ',').count();

        determineSeedLimits();

        seed_artists = getSeedArtists();
        seed_tracks = getSeedTracks();
    }

    public abstract String generatePlaylist() throws Exception;

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
     * Based on the number of seed genres provided set the limits for seed artists and seed tracks.
     * This is important as the recommendation endpoint only allows 5 seed values in any combination
     * of genres, tracks, and artists
     */
    private  void determineSeedLimits() {
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
     * Calls the recommendation endpoint and sorts the returned response by duration in ascending order
     *
     * @param limit number of songs to fetch
     * @param min_tempo min tempo of songs to fetch
     * @param max_tempo max tempo of songs to fetch
     * @param target_tempo target tempo of songs to fetch
     * @return TrackSimplified array of sorted tracks which were fetched by the recommendation endpoint
     * @throws GetRecommendationsException if an error occurs when fetching the recommendation
     */
    protected TrackSimplified[] getSortedRecommendations(int limit, float min_tempo, float max_tempo, float target_tempo)
            throws GetRecommendationsException {

        RecommendationArguments current_arguments = new RecommendationArguments(
                spotify_api, limit, genres, seed_artists, seed_tracks,
                min_tempo, max_tempo, target_tempo, user.getCountry());

        Recommendations recommendations = getRecommendations(current_arguments);

        TrackSimplified[] recommended_tracks = recommendations.getTracks();

        if(recommended_tracks == null) return null;

        Arrays.sort(recommended_tracks, duration_comparator);

        return recommended_tracks;
    }

    /**
     * Calls recommendation endpoint and sorts the returned response (excludes target tempo to capture a large range)
     *
     * @param limit number of songs to fetch
     * @param min_tempo min tempo of songs to fetch
     * @param max_tempo max tempo of songs to fetch
     * @return TrackSimplified array of sorted tracks which were fetched by the recommendation endpoint
     * @throws GetRecommendationsException if an error occurs when fetching the recommendation
     */
    protected TrackSimplified[] getSortedRecommendationRange(int limit, float min_tempo, float max_tempo)
            throws GetRecommendationsException {

        // Target tempo will not be used, 0 is used as a placeholder
        RecommendationArguments current_arguments = new RecommendationArguments(
                spotify_api, limit, genres, seed_artists, seed_tracks,
                min_tempo, max_tempo, 0, user.getCountry());

        Recommendations recommendations = getRecommendationTempoRange(current_arguments);

        TrackSimplified[] recommended_tracks = recommendations.getTracks();

        Arrays.sort(recommended_tracks, duration_comparator);

        return recommended_tracks;
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
}
