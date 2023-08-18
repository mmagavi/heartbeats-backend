package UtilitiesTests;

import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import SpotifyUtilities.PersonalizationUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import Server.Server;
import se.michaelthelin.spotify.model_objects.specification.Track;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PersonalizationUtilitiesTests {

    @BeforeAll
    public static void setup(){
        Server.spotify_api.setAccessToken(TestTokens.access_token);
        Server.spotify_api.setRefreshToken(TestTokens.refresh_token);
    }

    @Test
    public void passingNullToGetUsersTopArtistsThrowsException(){

        assertThrows(GetUsersTopArtistsRequestException.class, () -> {
            PersonalizationUtilities.getUsersTopArtists(null);
        });
    }

    @Test
    public void passingValidArgsToGetUsersTopArtistsDoesNotThrowException(){

        assertDoesNotThrow(() -> {
            Artist[] artists = PersonalizationUtilities.getUsersTopArtists(Server.spotify_api);

            for(Artist artist: artists){
                System.out.println(artist.getName());
            }
        });
    }

    @Test
    public void passingNullToGetUsersTopTracksThrowsException(){

        assertThrows(GetUsersTopTracksRequestException.class, () -> {
            PersonalizationUtilities.getUsersTopTracks(null);
        });
    }

    @Test
    public void passingValidArgsToGetUsersTopTracksDoesNotThrowException(){

        assertDoesNotThrow(() -> {
            Track[] tracks = PersonalizationUtilities.getUsersTopTracks(Server.spotify_api);

            for(Track track: tracks){
                System.out.println(track.getName());
            }
        });
    }
}
