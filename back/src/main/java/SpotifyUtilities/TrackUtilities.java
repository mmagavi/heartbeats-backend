package SpotifyUtilities;

import ExceptionClasses.TrackExceptions.GetAudioFeaturesForSeveralTracksException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;

public class TrackUtilities {
    private static final String[] ids = new String[]{"01iyCAUm8EvOFqVWYJ3dVX"};


    /**
     * Gets the audio features of several tracks (limit of 100)
     * @param spotify_api SpotifyApi object linked to the users account
     * @param track_ids ids of the tracks to query AudioFeature information on
     * @return AudioFeatures array
     * @throws GetAudioFeaturesForSeveralTracksException if an error occurs when accessing the spotify endpoint
     */
    public static AudioFeatures[] getAudioFeaturesForSeveralTracks(SpotifyApi spotify_api, String[] track_ids)
    throws GetAudioFeaturesForSeveralTracksException{

        try {

            GetAudioFeaturesForSeveralTracksRequest getAudioFeaturesForSeveralTracksRequest =
                    spotify_api.getAudioFeaturesForSeveralTracks(track_ids).build();

            return getAudioFeaturesForSeveralTracksRequest.execute();

        } catch (Exception ex) {

            throw new GetAudioFeaturesForSeveralTracksException(ex.getMessage());
        }
    }
}