package UtilitiesTests;

import ExceptionClasses.PlaylistExceptions.*;
import SpotifyUtilities.PlaylistUtilities;
import SpotifyUtilities.UserProfileUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.User;
import Server.Server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlaylistUtilitiesTests {

    @BeforeAll
    public static void setup(){
        Server.spotify_api.setAccessToken(TestTokens.access_token);
        Server.spotify_api.setRefreshToken(TestTokens.refresh_token);
    }

    @Test
    public void passingNullToCreatePlaylistThrowsException() {

        assertThrows(CreatePlaylistException.class, () -> {
            PlaylistUtilities.createPlaylist(null, "user_id", "user_name");
        });

        assertThrows(CreatePlaylistException.class, () -> {
            PlaylistUtilities.createPlaylist(null, "user_id", "user_name",
                    true, "name", "description");
        });
    }

    @Test
    public void passingValidArgsToCreatePlaylistsDoesNotThrowException() throws Exception{

        User user = UserProfileUtilities.getCurrentUsersProfile(Server.spotify_api);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime current_time = LocalDateTime.now();

        assertDoesNotThrow(() -> {
            PlaylistUtilities.createPlaylist(Server.spotify_api, user.getId(), user.getDisplayName(),
                    true,"heartBeasts test playlist " + current_time, "Test playlist");
        });
    }

    @Test
    public void passingNullToAddItemsToPlaylistThrowsException() {

        String[] song_uris = {"id1", "id2"};

        assertThrows(AddItemsToPlaylistException.class, () -> {
            PlaylistUtilities.addItemsToPlaylist(null, "playlist_id", song_uris);
        });
    }

    @Test
    public void passingValidArgsToAddItemsToPlaylistsDoesNotThrowException() throws Exception{

        String[] uris = {"spotify:track:3wGXyJGsCf1myH5MooQIqE", "spotify:track:4mmkhcEm1Ljy1U9nwtsxUo"};

        assertDoesNotThrow(() -> {
            PlaylistUtilities.addItemsToPlaylist(Server.spotify_api, "1tlqJ42jhWBFzVbJyzCvAj", uris);
        });
    }

    @Test
    public void passingNullToGetUsersPlaylistsThrowsException() {

        assertThrows(GetUsersPlaylistsException.class, () -> {
            PlaylistUtilities.getUsersPlaylists(null, 10);
        });
    }

    @Test
    public void passingValidArgsToGetUsersPlaylistsDoesNotThrowException() throws Exception{

        assertDoesNotThrow(() -> {
            PlaylistSimplified[] playlists = PlaylistUtilities.getUsersPlaylists(Server.spotify_api, 10);
            System.out.println("Number of Playlists retrieved: " + playlists.length);
        });
    }

    @Test
    public void passingNullToGetPlaylistItemsThrowsException() {

        assertThrows(GetPlaylistItemsException.class, () -> {
            PlaylistUtilities.getPlaylistItems(null, "playlist_id");
        });
    }

    @Test
    public void passingValidArgsToGetPlaylistItemsDoesNotThrowException() throws Exception{

        assertDoesNotThrow(() -> {
            PlaylistTrack[] tracks = PlaylistUtilities.getPlaylistItems(Server.spotify_api,
                    "1tlqJ42jhWBFzVbJyzCvAj");

            System.out.println("Number of items in playlist 1tlqJ42jhWBFzVbJyzCvAj: " + tracks.length);
        });
    }

}
