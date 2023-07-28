package ExceptionClasses.ProfileExceptions;

import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

public class GetCurrentUsersProfileException extends Exception{

    public GetCurrentUsersProfileException(String message){
        super(message);
    }
}
