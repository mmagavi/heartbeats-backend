package UtilitiesTests;

import ExceptionClasses.LibraryExceptions.GetUsersSavedAlbumsException;
import ExceptionClasses.LibraryExceptions.GetUsersSavedTracksException;
import SpotifyUtilities.LibraryUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.SavedAlbum;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import Server.Server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LibraryUtilitiesTests {

    @BeforeAll
    public static void setup(){
        Server.spotify_api.setAccessToken(TestTokens.access_token);
        Server.spotify_api.setRefreshToken(TestTokens.refresh_token);
    }

    @Test
    public void passingNullToGetUsersSavedTracksThrowsException() {

        assertThrows(GetUsersSavedTracksException.class, () -> {
            LibraryUtilities.getUsersSavedTracks(null);
        });
    }

    @Test
    public void passingValidArgsToGetUsersSavedTracksDoesNotThrowException() {

        assertDoesNotThrow(() -> {
            SavedTrack[] saved_tracks = LibraryUtilities.getUsersSavedTracks(Server.spotify_api);
            System.out.println("Number of saved tracks: " + saved_tracks.length);
        });
    }

    @Test
    public void passingNullToGetUsersSavedAlbumsThrowsException() {

        assertThrows(GetUsersSavedAlbumsException.class, () -> {
            LibraryUtilities.getUsersSavedAlbums(null);
        });
    }

    @Test
    public void passingValidArgsToGetUsersSavedAlbumsDoesNotThrowException() {

        assertDoesNotThrow(() -> {
            SavedAlbum[] saved_albums = LibraryUtilities.getUsersSavedAlbums(Server.spotify_api);
            System.out.println("Number of saved albums: " + saved_albums.length);
        });
    }


}
