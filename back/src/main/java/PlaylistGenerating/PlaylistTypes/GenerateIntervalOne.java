package PlaylistGenerating.PlaylistTypes;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Playlist;

public class GenerateIntervalOne extends GeneratePlaylist {

    /**
     * Constructor for generating a classic style playlist
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @param genres Desired Genres
     * @param age Age of the user
     * @param workout_length Length of the workout
     */
    public GenerateIntervalOne(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity){

        super(spotify_api, genres, age, workout_length, intensity);

    }

    @Override
    public String generatePlaylist() throws Exception {
        return null;
    }

    @Override
    protected int getTargetBPM() {
        return 0;
    }
}
