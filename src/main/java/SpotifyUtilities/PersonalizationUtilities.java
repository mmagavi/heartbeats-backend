package SpotifyUtilities;

import ExceptionClasses.LibraryExceptions.GetUsersSavedTracksException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;

public class PersonalizationUtilities {

    /**
     * Gets 5 of the user's top artists
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @return Artist array of the user's top artists
     * @throws GetUsersTopArtistsRequestException if an exception was thrown in getUsersSavedTracksRequest.execute()
     */
    public static Artist[] GetUsersTopArtists(SpotifyApi spotify_api) throws GetUsersTopArtistsRequestException {
        return GetUsersTopArtists(spotify_api, 5);
    }
    public static Artist[] GetUsersTopArtists(SpotifyApi spotify_api, int limit) throws GetUsersTopArtistsRequestException {

        try{

            GetUsersTopArtistsRequest getUsersTopArtistsRequest = spotify_api.getUsersTopArtists().limit(limit).build();

            Paging<Artist> artistPaging = getUsersTopArtistsRequest.execute();

            return artistPaging.getItems();

        }catch(Exception ex){

            throw new GetUsersTopArtistsRequestException(ex.getMessage());
        }
    }


    /**
     * Gets 5 of the user's top tracks
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @return Artist array of the user's top artists
     * @throws GetUsersTopTracksRequestException if an exception was thrown in getUsersSavedTracksRequest.execute()
     */
    public static Track[] GetUsersTopTracks(SpotifyApi spotify_api) throws GetUsersTopTracksRequestException {
        return GetUsersTopTracks(spotify_api, 5);
    }
    public static Track[] GetUsersTopTracks(SpotifyApi spotify_api, int limit) throws GetUsersTopTracksRequestException {
        try{

            GetUsersTopTracksRequest getUsersTopTracksRequest = spotify_api.getUsersTopTracks().limit(limit).build();

            Paging<Track> trackPaging = getUsersTopTracksRequest.execute();

            return trackPaging.getItems();

        }catch(Exception ex){

            throw new GetUsersTopTracksRequestException(ex.getMessage());
        }
    }

}
