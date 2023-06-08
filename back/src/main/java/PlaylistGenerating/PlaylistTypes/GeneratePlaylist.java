package PlaylistGenerating.PlaylistTypes;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Playlist;

abstract class GeneratePlaylist {

    // Desired genres
    protected final String genres;

    // The spotifyAPI containing the user's information
    protected final SpotifyApi spotifyApi;

    // Given age
    protected final int age;

    // Length of the workout in seconds
    protected final int workout_length;

    // Intensity of the workout
    protected final String intensity;

    protected final int resting_bpm = 69; // assuming an average resting bpm

    protected final int margin_of_error; // seconds a playlist can be off the target by and still be acceptable

    public GeneratePlaylist(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity) {

        this.spotifyApi = spotify_api;
        this.genres = genres;
        this.age = age;
        this.workout_length = workout_length * 60;
        this.intensity = intensity;
        this.margin_of_error = (int)(workout_length * .05);
    }

    public abstract Playlist generatePlaylist() throws Exception;

    protected abstract int getTargetBPM();


}
