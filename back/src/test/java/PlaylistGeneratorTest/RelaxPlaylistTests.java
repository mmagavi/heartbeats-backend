package PlaylistGeneratorTest;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.PlaylistExceptions.AddItemsToPlaylistException;
import ExceptionClasses.PlaylistExceptions.CreatePlaylistException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import PlaylistGenerating.HeartRateRange;
import PlaylistGenerating.PlaylistTypes.GenerateExercise;
import PlaylistGenerating.PlaylistTypes.GenerateRelax;
import Server.Server;
import SpotifyUtilities.infoUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RelaxPlaylistTests {
    private boolean isCorrect;


    @BeforeAll
    public static void setup() {
        String tkn = "BQAgcQyY1jEbveKqitr48m1jmnErReTq8hJ3MiyzoZ6U0Kaxf3EE-TS3R9ddcc_CEqFivcmUeEAScXOFseFsnYtxMdzoMKUw3E-ePpqXGLKu9W89f5q6_YFt1mXFWOF7ltcvgYTvck3R8ed2-gbFxh7JJZ535XAnnjkBoH28LpFRj0NYq9f1SCmVY-1snCQ60gW0t2rIoTJ4yPYOwTQlMddq-aF8Trc4xvILlnQRG1gZMAL8Tt1TXa3tIXXLwVLAbN0";
        String rfrshTkn = "AQBgivUKH2HiJE5DmuQ6v8DP4FV1cKaXN79pfZ2S-beKswTVgf19UgBYw77ot5v7WEfPinifr1xoJxWu4EnHV84niXrd3F_uHnj9xmudWTFGnKjA2mNCHekfNV47OILF0uA";
        Server.spotify_api.setAccessToken(tkn);
        Server.spotify_api.setRefreshToken(rfrshTkn);
    }
    private String[] getTrackIDs(LinkedList<String> songs) {
        String[] tracks = new String[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            String songURI = songs.get(i);
            String songID = songURI.substring(songURI.length()-22);
            tracks[i] = songID;
        }
        return tracks;
    }
    private boolean checkSongsInTempo(List<AudioFeatures> feats, HeartRateRange bpm) {
        for (AudioFeatures feat : feats) {
            float tempo = feat.getTempo();
            if (tempo > bpm.max_heart_rate() || tempo < bpm.min_heart_rate()) {
                throw new IllegalArgumentException("tempo "+tempo+" of song "+feat.getId()+" is out of bounds");
            }
        }
        return true;
    }
    @Test
    public void testRelaxBasic() throws GetCurrentUsersProfileException, AddItemsToPlaylistException, CreatePlaylistException, GetUsersTopArtistsRequestException, GetRecommendationsException, GetUsersTopTracksRequestException {
        GenerateRelax gr1 = new GenerateRelax( 90, 45, "pop",  Server.spotify_api);
        GenerateRelax gr2 = new GenerateRelax( 100, 30, "british",  Server.spotify_api);
        GenerateRelax gr3 = new GenerateRelax( 110, 70, "k-pop",  Server.spotify_api);





    }

    @Test
    public void testRelaxAlgo() {
        // Make sure playlist generates without errors
        Assertions.assertDoesNotThrow(() -> {
            GenerateRelax gr = new GenerateRelax( 100, 60, "pop",  Server.spotify_api);
        //Create heart rate ranges that are in this playlist^
            HeartRateRange h1 = new HeartRateRange(64, 68, 72);
            HeartRateRange h2 = new HeartRateRange(96, 100, 104);

        //Get the songs in the playlist's IDs at each HR range
            LinkedList<String> songs1 = gr.getSongs().get(h1);
            LinkedList<String> songs2 = gr.getSongs().get(h2);


            String[] tracks1 = this.getTrackIDs(songs1);
            String[] tracks2 = this.getTrackIDs(songs2);
            //Get the tempos for all songs

            List<AudioFeatures> feats1  =Arrays.asList( infoUtilities.getAudioFeaturesForSeveralTracks_Sync(Server.spotify_api, tracks1) );
            List<AudioFeatures> feats2  =Arrays.asList( infoUtilities.getAudioFeaturesForSeveralTracks_Sync(Server.spotify_api, tracks2) );
            //Check that all songs fall in the range
            Assertions.assertEquals(true,this.checkSongsInTempo(feats1, h1));
            Assertions.assertEquals(true,this.checkSongsInTempo(feats2, h2));





        });
    }
}
