package PlaylistGenerating.PlaylistTypes;

import ExceptionClasses.ArtistExceptions.GetSeveralArtistsException;
import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import PlaylistGenerating.EnergyRanges.EnergyRange;
import PlaylistGenerating.HeartRateRanges.TargetHeartRateRange;
import SpotifyUtilities.PersonalizationUtilities;
import SpotifyUtilities.RecommendationArguments;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.Arrays;

import static PlaylistGenerating.EnergyRanges.DesiredEnergyRanges.getEnergyRange;
import static PlaylistGenerating.HeartRateRanges.DesiredHeartRateRanges.getTargetHeartRateRange;
import static PlaylistGenerating.PlaylistTypes.CommonUtilities.*;
import static SpotifyUtilities.ArtistUtilities.getSeveralArtists;
import static SpotifyUtilities.BrowsingUtilities.*;
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
    protected final int resting_bpm = 80; // assuming an average resting bpm
    protected final float margin_of_error; // percent a playlist can be off the target by and still be acceptable
    protected final float avg_song_len = 3.5f;
    protected int seed_genres_provided = 0;
    protected int desired_num_seed_artists;
    protected int desired_num_seed_tracks;
    protected int tracks_per_interval;
    protected String seed_artists;
    protected String seed_tracks;
    protected final int target_bpm;
    protected final float starting_energy = .4f;
    protected final float energy_offset = .05f;
    protected final float target_energy;

    protected User user;
    protected final boolean is_personalized;

    public GeneratePlaylist(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity,
                            boolean is_personalized)
            throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException,
            GetCurrentUsersProfileException, GetSeveralArtistsException {

        this.spotify_api = spotify_api;
        this.genres = genres;
        this.age = age;
        this.workout_length_min = workout_length;
        this.workout_len_ms = workout_length * 60_000;
        this.intensity = intensity;
        this.margin_of_error = .02f;
        this.user = getCurrentUsersProfile(spotify_api);
        this.is_personalized = is_personalized;

        target_bpm = getTargetBPM();
        target_energy = getTargetEnergy();

        // Only 5 seed values across tracks, artists, and genres can be provided to the recommendations endpoint
        // so if less than the max of 3 genres the front end lets the user choose from were chosen we can provide
        // additional seed artists or seed tracks
        seed_genres_provided = (int) genres.chars().filter(ch -> ch == ',').count();

        determineSeedLimits();

        System.out.println("seed_genres_provided: " + seed_genres_provided);
        System.out.println("desired_num_seed_artists: " + desired_num_seed_artists);
        System.out.println("desired_num_seed_tracks: " + desired_num_seed_tracks);

        seed_artists = getSeedArtists();
        seed_tracks = getSeedTracks();

        System.out.println("seed_artists: " + seed_artists);
        System.out.println("seed_tracks: " + seed_tracks);
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

    private float getTargetEnergy(){

        EnergyRange energy_range = getEnergyRange(this.age);

        switch (intensity) {
            case "low" -> {
                return energy_range.low_intensity_target();
            }
            case "medium" -> {
                return energy_range.medium_intensity_target();
            }
            case "high" -> {
                return energy_range.high_intensity_target();
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
    private void determineSeedLimits() {

        if (!is_personalized) {
            desired_num_seed_artists = 0;
            desired_num_seed_tracks = 0;

            return;
        }

        switch (seed_genres_provided) {
            case 0 -> {
                desired_num_seed_artists = 3;
                desired_num_seed_tracks = 2;
            }
            case 1 -> {
                desired_num_seed_artists = 2;
                desired_num_seed_tracks = 2;
            }
            case 2 -> {
                desired_num_seed_artists = 2;
                desired_num_seed_tracks = 1;
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
     * @param limit        number of songs to fetch
     * @param min_tempo    min tempo of songs to fetch
     * @param max_tempo    max tempo of songs to fetch
     * @param target_tempo target tempo of songs to fetch
     * @return TrackSimplified array of sorted tracks which were fetched by the recommendation endpoint
     * @throws GetRecommendationsException if an error occurs when fetching the recommendation
     */
    protected TrackSimplified[] getSortedRecommendations(int limit, float min_tempo, float max_tempo, float target_tempo, float energy)
            throws GetRecommendationsException {


        TrackSimplified[] recommended_tracks = getUnsortedRecommendations(limit, min_tempo, max_tempo, target_tempo, energy);

        if (recommended_tracks == null) return null;

        Arrays.sort(recommended_tracks, duration_comparator);

        return recommended_tracks;
    }

    protected TrackSimplified[] getSortedRecommendations(int limit, float min_tempo, float max_tempo,
                                                         float target_tempo, float min_energy, float max_energy,float energy)
            throws GetRecommendationsException {


        TrackSimplified[] recommended_tracks = getUnsortedRecommendations(limit, min_tempo, max_tempo, target_tempo, energy);

        if (recommended_tracks == null) return null;

        Arrays.sort(recommended_tracks, duration_comparator);

        return recommended_tracks;
    }

    /**
     * Calls the recommendation endpoint and sorts the returned response by duration in ascending order
     *
     * @param limit        number of songs to fetch
     * @param min_tempo    min tempo of songs to fetch
     * @param max_tempo    max tempo of songs to fetch
     * @param target_tempo target tempo of songs to fetch
     * @return TrackSimplified array of sorted tracks which were fetched by the recommendation endpoint
     * @throws GetRecommendationsException if an error occurs when fetching the recommendation
     */
    protected TrackSimplified[] getUnsortedRecommendations(int limit, float min_tempo, float max_tempo, float target_tempo, float energy)
            throws GetRecommendationsException {

//        System.out.println("genres: " + genres);
//        System.out.println("seed_artists: " + seed_artists);
//        System.out.println("seed_tracks: " + seed_tracks);

        RecommendationArguments current_arguments = new RecommendationArguments(
                spotify_api, limit, genres, seed_artists, seed_tracks,
                min_tempo, max_tempo, target_tempo, energy - energy_offset, energy + energy_offset,
                energy, user.getCountry());

        Recommendations recommendations;

        recommendations = getRecommendations(current_arguments);

        return recommendations.getTracks();
    }

    protected TrackSimplified[] getUnsortedRecommendations(int limit, float min_tempo, float max_tempo,
                                                           float target_tempo, float min_energy, float max_energy, float energy)
            throws GetRecommendationsException {

//        System.out.println("genres: " + genres);
//        System.out.println("seed_artists: " + seed_artists);
//        System.out.println("seed_tracks: " + seed_tracks);

        RecommendationArguments current_arguments = new RecommendationArguments(
                spotify_api, limit, genres, seed_artists, seed_tracks,
                min_tempo, max_tempo, target_tempo, min_energy, max_energy, energy, user.getCountry());

        Recommendations recommendations;

        recommendations = getRecommendations(current_arguments);

        return recommendations.getTracks();
    }

    /**
     * Calls the recommendation endpoint and sorts the returned response by duration in ascending order
     * (ONLY USES GENRE)
     *
     * @param limit        number of songs to fetch
     * @param min_tempo    min tempo of songs to fetch
     * @param max_tempo    max tempo of songs to fetch
     * @param target_tempo target tempo of songs to fetch
     * @return TrackSimplified array of sorted tracks which were fetched by the recommendation endpoint
     * @throws GetRecommendationsException if an error occurs when fetching the recommendation
     */
    protected TrackSimplified[] getUnsortedGenreRecommendations(int limit, float min_tempo, float max_tempo, float target_tempo, float energy)
            throws GetRecommendationsException {

        // Don't need seed artists and tracks as they will not be accessed so passing null is okay here
        RecommendationArguments current_arguments = new RecommendationArguments(
                spotify_api, limit, genres, null, null, min_tempo,
                max_tempo, target_tempo, energy - energy_offset, energy + energy_offset,
                energy, user.getCountry());

        Recommendations recommendations = getGenreRecommendations(current_arguments);

        return recommendations.getTracks();
    }

    /**
     * Calls recommendation endpoint and sorts the returned response (excludes target tempo to capture a large range)
     *
     * @param limit     number of songs to fetch
     * @param min_tempo min tempo of songs to fetch
     * @param max_tempo max tempo of songs to fetch
     * @return TrackSimplified array of sorted tracks which were fetched by the recommendation endpoint
     * @throws GetRecommendationsException if an error occurs when fetching the recommendation
     */
    protected TrackSimplified[] getSortedRecommendationRange(int limit, float min_tempo, float max_tempo, float energy)
            throws GetRecommendationsException {

        // Target tempo will not be used, 0 is used as a placeholder
        RecommendationArguments current_arguments = new RecommendationArguments(
                spotify_api, limit, genres, seed_artists, seed_tracks,
                min_tempo, max_tempo, 0, energy - energy_offset, energy + energy_offset,
                energy, user.getCountry());

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
    private String getSeedTracks() throws GetUsersTopTracksRequestException, GetSeveralArtistsException {

        if (desired_num_seed_tracks == 0) return "";

        int num_tracks_found = 0;

        Track[] seed_tracks = new Track[desired_num_seed_tracks]; // store final results here
        createNullArray(seed_tracks, desired_num_seed_tracks);

        // Splitting up the genres, so we can work with them easier, we will be checking against this list
        String[] selected_genres = genres.split(",");

        Track[] top_tracks = PersonalizationUtilities.getUsersTopTracks(spotify_api);

        for (Track track : top_tracks) {

            if (num_tracks_found == desired_num_seed_tracks) break;

            // Can only check genre with an Artist object, so grab all the artists on the current track
            ArtistSimplified[] simplified_artists = track.getArtists();
            String[] ids = getArtistIDs(simplified_artists);
            Artist[] artists = getSeveralArtists(spotify_api, ids);


            // If one of the artists on the track is described as being in a genre the user selected, add the track
            for (Artist artist : artists) {

                String[] artist_genres = artist.getGenres();
                removeGenreDashes(selected_genres);

                for (String genre : artist_genres) {

                    for (String selected_genre : selected_genres) {

                        if (genre.contains(selected_genre)) {

                            // num_tracks_found acts as an index as its value is always the index of the next track to add
                            // when 0 found, add at 0. 2 track found add the next at index 2 and so on.
                            seed_tracks[num_tracks_found] = track;
                            num_tracks_found++;

                            if(num_tracks_found == desired_num_seed_tracks){
                                return createCommaSeperatedString(seed_tracks);
                            }
                        }
                    }
                }
            }
        }

        // If no tracks were added the 0 element will still be null
        if (seed_tracks[0] == null) {
            return "";
        }

        return createCommaSeperatedString(seed_tracks);
    }

    /**
     * Gets the users top artist(s) and returns a comma seperated string of their IDS
     *
     * @return comma seperated string of IDs of the users top artist(s)
     * @throws GetUsersTopArtistsRequestException if an error occurs fetching the users top artists
     */
    private String getSeedArtists() throws GetUsersTopArtistsRequestException {

        if (desired_num_seed_artists == 0) return "";

        int num_artists_found = 0;

        Artist[] seed_artists = new Artist[desired_num_seed_artists]; // store final results here
        createNullArray(seed_artists, desired_num_seed_artists);

        // Splitting up the genres, so we can work with them easier, we will be checking against this list
        String[] selected_genres = genres.split(",");

        Artist[] top_artists = PersonalizationUtilities.getUsersTopArtists(spotify_api);

        for (Artist artist : top_artists) {

            String[] artist_genres = artist.getGenres();
            removeGenreDashes(selected_genres);


            for (String genre : artist_genres) {

                for (String selected_genre : selected_genres) {

                    if (genre.contains(selected_genre)) {

                        // num_artists_found acts as an index as its value is always the index of the next artist to add
                        // when 0 found, add at 0. 2 artists found add the next at index 2 and so on.
                        seed_artists[num_artists_found] = artist;
                        num_artists_found++;

                        if (num_artists_found == desired_num_seed_artists) {
                            return createCommaSeperatedString(seed_artists);
                        }
                    }
                }
            }
        }

        // If no artists were added the 0 element will still be null
        if (seed_artists[0] == null) {
            return "";
        }

        return createCommaSeperatedString(seed_artists);
    }
}