package SpotifyUtilities;

import ExceptionClasses.ArtistExceptions.GetSeveralArtistsException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.artists.GetSeveralArtistsRequest;

import java.io.IOException;

public class ArtistUtilities {

    /**
     * Gets several artists based on the provided ids
     * @param spotifyApi spotify api object needed for api calls
     * @param ids ids of the artists
     * @return Artist array of the requested artists
     * @throws GetSeveralArtistsException if an error was encountered in the api call
     */
    public static Artist[] getSeveralArtists(SpotifyApi spotifyApi, String[] ids) throws GetSeveralArtistsException {

        try {
            GetSeveralArtistsRequest getSeveralArtistsRequest = spotifyApi.getSeveralArtists(ids)
                    .build();

            return getSeveralArtistsRequest.execute();

        } catch (Exception ex) {

            throw new GetSeveralArtistsException(ex.getMessage());
        }

    }
}
