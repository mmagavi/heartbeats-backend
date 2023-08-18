package SpotifyUtilities;

import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;

public class PersonalizationUtilities {

    /**
     * Gets all the user's top artists
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @return Artist array of the user's top artists
     * @throws GetUsersTopArtistsRequestException if an exception was thrown in getUsersSavedTracksRequest.execute()
     */
    public static Artist[] getUsersTopArtists(SpotifyApi spotify_api) throws GetUsersTopArtistsRequestException {
        try{

            GetUsersTopArtistsRequest getUsersTopArtistsRequest = spotify_api.getUsersTopArtists().build();

            Paging<Artist> artistPaging = getUsersTopArtistsRequest.execute();

            return artistPaging.getItems();

        }catch(Exception ex){

            throw new GetUsersTopArtistsRequestException(ex.getMessage());
        }
    }
    public static Artist[] getUsersTopArtists(SpotifyApi spotify_api, int limit) throws GetUsersTopArtistsRequestException {

        try{

            GetUsersTopArtistsRequest getUsersTopArtistsRequest = spotify_api.getUsersTopArtists().limit(limit).build();

            Paging<Artist> artistPaging = getUsersTopArtistsRequest.execute();

            return artistPaging.getItems();

        }catch(Exception ex){

            throw new GetUsersTopArtistsRequestException(ex.getMessage());
        }
    }


    /**
     * Gets all the user's top tracks
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @return Artist array of the user's top artists
     * @throws GetUsersTopTracksRequestException if an exception was thrown in getUsersSavedTracksRequest.execute()
     */
    public static Track[] getUsersTopTracks(SpotifyApi spotify_api) throws GetUsersTopTracksRequestException {
        try{

            GetUsersTopTracksRequest getUsersTopTracksRequest = spotify_api.getUsersTopTracks().build();

            Paging<Track> trackPaging = getUsersTopTracksRequest.execute();

            return trackPaging.getItems();

        }catch(Exception ex){

            throw new GetUsersTopTracksRequestException(ex.getMessage());
        }
    }

    public static Track[] getUsersTopTracks(SpotifyApi spotify_api, int limit) throws GetUsersTopTracksRequestException {
        try{

            GetUsersTopTracksRequest getUsersTopTracksRequest = spotify_api.getUsersTopTracks().limit(limit).build();

            Paging<Track> trackPaging = getUsersTopTracksRequest.execute();

            return trackPaging.getItems();

        }catch(Exception ex){

            throw new GetUsersTopTracksRequestException(ex.getMessage());
        }
    }

}
