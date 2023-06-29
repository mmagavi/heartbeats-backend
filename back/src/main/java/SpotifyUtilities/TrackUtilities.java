package SpotifyUtilities;

import ExceptionClasses.TrackExceptions.GetAudioFeaturesForSeveralTracksException;
import ExceptionClasses.TrackExceptions.GetAudioFeaturesForTrackException;
import ExceptionClasses.TrackExceptions.GetTrackException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.util.Comparator;

public class TrackUtilities {
    private static final String[] ids = new String[]{"01iyCAUm8EvOFqVWYJ3dVX"};

    // Compares tracks by duration which we want to sort all tracks by (in ascending order)
    public static final Comparator<TrackSimplified> duration_comparator =
            (TrackSimplified track_one, TrackSimplified track_two)
                    -> Integer.compare(track_one.getDurationMs(), track_two.getDurationMs());


    /**
     * Gets the audio features of several tracks (limit of 100)
     *
     * @param spotify_api SpotifyApi object linked to the users account
     * @param track_ids   ids of the tracks to query AudioFeature information on
     * @return AudioFeatures array
     * @throws GetAudioFeaturesForSeveralTracksException if an error occurs when accessing the spotify endpoint
     */
    public static AudioFeatures[] getAudioFeaturesForSeveralTracks(SpotifyApi spotify_api, String[] track_ids)
            throws GetAudioFeaturesForSeveralTracksException {

        try {

            GetAudioFeaturesForSeveralTracksRequest getAudioFeaturesForSeveralTracksRequest =
                    spotify_api.getAudioFeaturesForSeveralTracks(track_ids).build();

            return getAudioFeaturesForSeveralTracksRequest.execute();

        } catch (Exception ex) {

            throw new GetAudioFeaturesForSeveralTracksException(ex.getMessage());
        }
    }

    /**
     * Gets the audio features of several tracks (limit of 100)
     *
     * @param spotify_api SpotifyApi object linked to the users account
     * @param track_id   id of the track to query AudioFeature information on
     * @return AudioFeatures array
     * @throws GetAudioFeaturesForTrackException if an error occurs when accessing the spotify endpoint
     */
    public static AudioFeatures getAudioFeaturesForTrack(SpotifyApi spotify_api, String track_id)
            throws GetAudioFeaturesForTrackException {

        try {

            GetAudioFeaturesForTrackRequest getAudioFeaturesForTrackRequest =
                    spotify_api.getAudioFeaturesForTrack(track_id).build();

            return getAudioFeaturesForTrackRequest.execute();

        } catch (Exception ex) {

            throw new GetAudioFeaturesForTrackException(ex.getMessage());
        }
    }

    /**
     * Gets a Track based on the provided track_id
     * @param spotify_api SpotifyApi object linked to the users account
     * @param track_id   id of the track to fetch
     * @return Track object based on the provided track id
     * @throws GetTrackException if an error occurs in the API call process
     */
    public static Track getTrack(SpotifyApi spotify_api, String track_id) throws GetTrackException {

        try {

            GetTrackRequest getTrackRequest = spotify_api.getTrack(track_id).build();

            return getTrackRequest.execute();

        } catch (Exception ex) {
            throw new GetTrackException(ex.getMessage());
        }
    }
}