package UtilitiesTests;

import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import SpotifyUtilities.UserProfileUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.User;
import Server.Server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserProfileUtilitiesTests {

    @BeforeAll
    public static void setup(){
        Server.spotify_api.setAccessToken(TestTokens.access_token);
        Server.spotify_api.setRefreshToken(TestTokens.refresh_token);
    }

    @Test
    public void passingNullToGetCurrentUsersProfileThrowsException() {

        assertThrows(GetCurrentUsersProfileException.class, () -> {
            UserProfileUtilities.getCurrentUsersProfile(null);
        });
    }

    @Test
    public void passingValidArgsToGetCurrentUsersProfileDoesNotThrowException() {

        assertDoesNotThrow(() -> {
            System.out.println(Server.spotify_api.getAccessToken());
            User user = UserProfileUtilities.getCurrentUsersProfile(Server.spotify_api);

            System.out.println(user.getDisplayName());
            System.out.println(user.getEmail());
            System.out.println(user.getId());
        });
    }
}
