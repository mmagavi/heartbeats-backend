package PlaylistGenerating.PlaylistTypes;

import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import PlaylistGenerating.TargetHeartRateRange;
import SpotifyUtilities.PlaylistUtilities;
import SpotifyUtilities.RecommendationArguments;
import SpotifyUtilities.TrackUtilities;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import static PlaylistGenerating.DesiredHeartRateRanges.getTargetHeartRateRange;
import static SpotifyUtilities.BrowsingUtilities.getRecommendations;
import static SpotifyUtilities.PersonalizationUtilities.GetUsersTopArtists;
import static SpotifyUtilities.PersonalizationUtilities.GetUsersTopTracks;
import static SpotifyUtilities.PlaylistUtilities.createPlaylist;
import static SpotifyUtilities.UserProfileUtilities.getCurrentUsersProfile;

public class GenerateClassic extends GeneratePlaylist {

    private int warmup_wind_down_length;
    private String seed_artists;
    private int seed_genres_provided;
    private int desired_num_seed_artists;
    private int desired_num_seed_tracks;
    private int target_bpm;

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
        warmup_wind_down_length = (int) (workout_length * .1);

        // Only 5 seed values across tracks, artists, and genres can be provided to the recommendations endpoint
        // so if less than the max of 3 genres the front end lets the user choose from were chosen we can provide
        // additional seed artists
        seed_genres_provided = (int) genres.chars().filter(ch -> ch == ',').count();

        determineSeedLimits();

    }

    @Override
    public String generatePlaylist() throws Exception {

        User user = getCurrentUsersProfile(spotify_api);

        // Create a playlist on the user's account
        Playlist playlist = createPlaylist(spotify_api, user.getId(), user.getDisplayName());

        String playlist_id = playlist.getId();

        String[] warmup_track_ids = getWarmupTracks();

        PlaylistUtilities.addItemsToPlaylist(spotify_api, playlist_id, warmup_track_ids);

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
     * @return comma seperated string of IDs of the users top track(s)
     * @throws GetUsersTopTracksRequestException if an error occurs fetching the users top tracks
     */
    private String getSeedTracks() throws GetUsersTopTracksRequestException {

        Track[] seed_tracks = GetUsersTopTracks(spotify_api, desired_num_seed_tracks);

        StringBuilder string_builder = new StringBuilder();

        // Will flip to true when there is more than one artist is seed_artists so they can be comma seperated
        boolean flag = false;

        for (Track track : seed_tracks) {

            // This will be false for the first iteration preventing a leading comma but true for all the rest
            if(flag){
                string_builder.append(",");
            }

            string_builder.append(track.getId());

            flag = true;
        }

        return string_builder.toString();
    }

    /**
     * Gets the users top artist(s) and returns a comma seperated string of their IDS
     * @return comma seperated string of IDs of the users top artist(s)
     * @throws GetUsersTopArtistsRequestException if an error occurs fetching the users top artists
     */
    private String getSeedArtists() throws GetUsersTopArtistsRequestException {

        Artist[] seed_artists = GetUsersTopArtists(spotify_api, desired_num_seed_artists);

        StringBuilder string_builder = new StringBuilder();

        // Will flip to true when there is more than one artist is seed_artists so they can be comma seperated
        boolean flag = false;

        for (Artist artist : seed_artists) {

            // This will be false for the first iteration preventing a leading comma but true for all the rest
            if(flag){
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
     * @return String Array of track ids for the warmup sequence
     * @throws Exception
     */
    private String[] getWarmupTracks() throws Exception {

        String seed_artists = getSeedArtists();
        String seed_tracks = getSeedTracks();

        // This bpm is halfway between the resting and target
        int midpoint_bpm = (target_bpm - resting_bpm) / 2;

        int limit = 30; // Number of tracks we want to get

        RecommendationArguments warmup_arguments = new RecommendationArguments(
                spotify_api, limit, genres, seed_artists, seed_tracks, resting_bpm, target_bpm, midpoint_bpm);

        // Get songs for the warmup
        Recommendations recommendations = getRecommendations(warmup_arguments);
        TrackSimplified[] recommended_tracks = recommendations.getTracks();

        // Get all the track ids as they are needed in the audio features request below
        String[] track_ids = getTrackIDs(recommended_tracks, limit);

        AudioFeatures[] track_features = TrackUtilities.getAudioFeaturesForSeveralTracks(spotify_api, track_ids);

        for(AudioFeatures track_feature: track_features){
            System.out.println(track_feature.getTempo());
        }

//        int song_length = recommended_tracks[0].getDurationMs() / 1000;

        int num_warmup_songs = warmup_wind_down_length / 3;

        String[] selected_song_ids = new String[num_warmup_songs];

        return selected_song_ids;
    }

    /**
     * Loops through all the given tracks and stores their IDs in a string array which is then returned
     * @param tracks array of tracks to fetch the id from
     * @param limit number of tracks in the array
     * @return String array of all the given track's ids
     */
    private String[] getTrackIDs(TrackSimplified[] tracks, int limit){

        String[] ids = new String[limit];

        for(int index = 0; index < limit; index++){
            ids[index] = tracks[index].getId();
        }

        return ids;
    }
}
