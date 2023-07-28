package SpotifyUtilities;

import ExceptionClasses.LibraryExceptions.GetUsersSavedAlbumsException;
import ExceptionClasses.LibraryExceptions.GetUsersSavedTracksException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.SavedAlbum;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import se.michaelthelin.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import se.michaelthelin.spotify.requests.data.library.GetUsersSavedTracksRequest;

public class LibraryUtilities {

    /**
     * Gets all the users saved tracks (Their liked songs)
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @return SavedTrack array of the user's saved tracks (Their liked songs)
     * @throws GetUsersSavedTracksException if an exception was thrown in getUsersSavedTracksRequest.execute()
     */
    public static SavedTrack[] getUsersSavedTracks(SpotifyApi spotify_api) throws GetUsersSavedTracksException {

        try{

            GetUsersSavedTracksRequest getUsersSavedTracksRequest = spotify_api.getUsersSavedTracks().build();

            Paging<SavedTrack> savedTrackPaging = getUsersSavedTracksRequest.execute();

            return savedTrackPaging.getItems();

        }catch(Exception ex){

            throw new GetUsersSavedTracksException(ex.getMessage());
        }
    }

    /**
     * Gets all the users saved tracks (Their liked songs)
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @return SavedTrack array of the user's saved tracks (Their liked songs)
     * @throws GetUsersSavedAlbumsException if an exception was thrown in getUsersSavedAlbumsRequest.execute()
     */
    public static SavedAlbum[] getUsersSavedAlbums(SpotifyApi spotify_api) throws GetUsersSavedAlbumsException {

        try{

            GetCurrentUsersSavedAlbumsRequest getCurrentUsersSavedAlbumsRequest =
                    spotify_api.getCurrentUsersSavedAlbums().build();

            Paging<SavedAlbum> savedAlbumPaging = getCurrentUsersSavedAlbumsRequest.execute();

            return savedAlbumPaging.getItems();

        }catch(Exception ex){

            throw new GetUsersSavedAlbumsException(ex.getMessage());
        }
    }
}
