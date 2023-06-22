package PlaylistGenerating.PlaylistTypes;

import se.michaelthelin.spotify.SpotifyApi;

abstract public class GeneratePlaylist {

    // Desired genres
    protected final String genres;

    // The spotifyAPI containing the user's information
    protected final SpotifyApi spotify_api;

    // Given age
    protected final int age;

    protected final int workout_length_min;

    // Length of the workout in MilliSeconds
    protected final int workout_length_ms;

    // Intensity of the workout
    protected final String intensity;

    protected final int resting_bpm = 69; // assuming an average resting bpm

    protected final float margin_of_error; // percent a playlist can be off the target by and still be acceptable

    public GeneratePlaylist(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity) {

        this.spotify_api = spotify_api;
        this.genres = genres;
        this.age = age;
        this.workout_length_min = workout_length;
        this.workout_length_ms = workout_length * 60_000;
        this.intensity = intensity;
        this.margin_of_error = .02f;



    }

    public abstract String generatePlaylist() throws Exception;

    protected abstract int getTargetBPM();


}
