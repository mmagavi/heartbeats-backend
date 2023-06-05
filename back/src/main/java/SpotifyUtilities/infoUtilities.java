package SpotifyUtilities;

import ExceptionClasses.InfoExceptions.GetAudioFeaturesForSeveralTracks_SyncException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class infoUtilities {
    private static final String[] ids = new String[]{"01iyCAUm8EvOFqVWYJ3dVX"};


    public static AudioFeatures[] getAudioFeaturesForSeveralTracks_Sync(SpotifyApi spotifyApi, String[] ids) throws GetAudioFeaturesForSeveralTracks_SyncException {
        try {
            final AudioFeatures[] audioFeatures = spotifyApi.getAudioFeaturesForSeveralTracks(ids).build().execute();
            return audioFeatures;

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new GetAudioFeaturesForSeveralTracks_SyncException("try again");
        }
    }
}

//    public static void getAudioFeaturesForSeveralTracks_Async() {
//        try {
//            final CompletableFuture<AudioFeatures[]> audioFeaturesFuture = getAudioFeaturesForSeveralTracksRequest.executeAsync();
//
//            // Thread free to do other tasks...
//
//            // Example Only. Never block in production code.
//            final AudioFeatures[] audioFeatures = audioFeaturesFuture.join();
//
//            System.out.println("Length: " + audioFeatures.length);
//        } catch (CompletionException e) {
//            System.out.println("Error: " + e.getCause().getMessage());
//        } catch (CancellationException e) {
//            System.out.println("Async operation cancelled.");
//        }
//    }

