package SpotifyUtilities;

import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

public class UserProfileUtilities {

    /**
     * Fetches the current user's profile information which includes:
     * - Birthdate
     * - Country
     * - Email
     * - Display Name
     * - Followers
     * - And a few others
     *
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @return User object
     * @throws GetCurrentUsersProfileException if an exception was thrown in getCurrentUsersProfileRequest.execute()
     */
    public static User getCurrentUsersProfile(SpotifyApi spotify_api) throws GetCurrentUsersProfileException{

        User user;


        try {
            GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotify_api.getCurrentUsersProfile().build();

            user = getCurrentUsersProfileRequest.execute();
            return user;
        }
        catch (Exception ex){

            throw new GetCurrentUsersProfileException(ex.getMessage());
        }
    }
}
