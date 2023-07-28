package ExceptionClasses.PlaylistExceptions;

import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;

public class CreatePlaylistException extends Exception{
    public CreatePlaylistException(String message){
        super(message);
    }
}
