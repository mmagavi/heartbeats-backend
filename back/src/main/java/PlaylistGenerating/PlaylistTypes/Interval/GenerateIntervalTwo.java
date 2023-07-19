package PlaylistGenerating.PlaylistTypes.Interval;

import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import PlaylistGenerating.PlaylistTypes.GeneratePlaylist;
import se.michaelthelin.spotify.SpotifyApi;

public class GenerateIntervalTwo extends GenerateIntervalOne {

    /**
     * Constructor for generating a classic style playlist
     *
     * @param spotify_api    SpotifyApi object that has been built with the current user's access token
     * @param genres         Desired Genres
     * @param age            Age of the user
     * @param workout_length Length of the workout
     * @param intensity
     */
    public GenerateIntervalTwo(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity) throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException, GetCurrentUsersProfileException {
        super(spotify_api, genres, age, workout_length, intensity);
    }


}