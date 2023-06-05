package PlaylistGeneratorTest;

import PlaylistGenerating.HeartRateRange;
import PlaylistGenerating.PlaylistTypes.GenerateExercise;
import Server.Server;
import SpotifyUtilities.infoUtilities;
import UtilitiesTests.TestTokens;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ExercisePlaylistTests {


    @BeforeAll
    public static void setup() {
        Server.spotify_api.setAccessToken(TestTokens.access_token);
        Server.spotify_api.setRefreshToken(TestTokens.refresh_token);
    }

    @Test
    public void testExerciseBasic() {
        // is a playlist w/ short end and start successfully created with acceptable parameters
        Assertions.assertDoesNotThrow(() -> {
            new GenerateExercise(21, 60, true, true, 30, "pop", Server.spotify_api);
            new GenerateExercise(10, 80, true, true, 25, "british", Server.spotify_api);
            new GenerateExercise(-10, 90, true, true, 25, "kpop", Server.spotify_api);
        });
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
    public void testExerciseHalfFull() {
        // is a playlist w/ either full end or start successfully created with acceptable parameters

        Assertions.assertDoesNotThrow(() -> {
            GenerateExercise gef1 = new GenerateExercise(21, 60, false, true, 30, "pop", Server.spotify_api);
            GenerateExercise gef2 = new GenerateExercise(21, 60, true, false, 30, "pop", Server.spotify_api);

            HeartRateRange h = new HeartRateRange(91,99,107);

            LinkedList<String> songs1 = gef1.getSongs().get(h);
            LinkedList<String> songs2 = gef2.getSongs().get(h);

            String[] tracks1 = this.getTrackIDs(songs1);
            String[] tracks2 = this.getTrackIDs(songs2);

            Assertions.assertTrue(tracks1.length > 0);
            Assertions.assertTrue(tracks2.length > 0);

            List<AudioFeatures> feats1 =Arrays.asList( infoUtilities.getAudioFeaturesForSeveralTracks_Sync(Server.spotify_api, tracks1) );
            List<AudioFeatures> feats2 =Arrays.asList( infoUtilities.getAudioFeaturesForSeveralTracks_Sync(Server.spotify_api, tracks2) );

            Assertions.assertTrue(checkSongsInTempo(feats1, h));
            Assertions.assertTrue(checkSongsInTempo(feats2, h));

            });

    }
    @Test
    public void testExerciseFull() {
        // is a playlist w/ either full end or start successfully created with acceptable parameters

        Assertions.assertDoesNotThrow(() -> {
            GenerateExercise gef1 = new GenerateExercise(21, 60, false, false, 30, "pop", Server.spotify_api);
            HeartRateRange h1 = new HeartRateRange(88, 96, 104);

            // getting the recommended songs in that range
            LinkedList<String> songs = gef1.getSongs().get(h1);
            String[] tracks = this.getTrackIDs(songs);

            // getting it as a list of info
            List<AudioFeatures> info = Arrays.asList( infoUtilities.getAudioFeaturesForSeveralTracks_Sync(Server.spotify_api,tracks) );
            Assertions.assertTrue(checkSongsInTempo(info, h1));
        });
    }













    }

