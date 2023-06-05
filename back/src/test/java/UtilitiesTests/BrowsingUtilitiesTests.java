package UtilitiesTests;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import SpotifyUtilities.BrowsingUtilities;
import SpotifyUtilities.RecommendationArguments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import Server.Server;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BrowsingUtilitiesTests {

    @BeforeAll
    public static void setup(){
        Server.spotify_api.setAccessToken(TestTokens.access_token);
        Server.spotify_api.setRefreshToken(TestTokens.refresh_token);
    }


    @Test
    public void passingNullToGetRecommendationsThrowsException(){

        RecommendationArguments arguments = new RecommendationArguments();

        arguments.spotify_api = null;
        arguments.limit = 10;
//        arguments.market = CountryCode.US;
//        arguments.min_popularity = 10;
//        arguments.max_popularity = 20;
//        arguments.target_popularity = 25;
        arguments.seed_genres = "hip-hop";
//        arguments.seed_artists = "15iVAtD3s3FsQR4w1v6M0P";
//        arguments.seed_tracks = "3WcC6NH9J77xPEvj1SOL7z";
        arguments.min_tempo = 100;
        arguments.max_tempo = 140;
        arguments.target_tempo = 120;
//        arguments.min_energy = .4f;
//        arguments.max_energy = .6f;
//        arguments.target_energy = .5f;

        assertThrows(GetRecommendationsException.class, () -> {
            BrowsingUtilities.getRecommendations(arguments);
        });
    }

    @Test
    public void passingValidArgsToGetRecommendationsDoesNotThrowException(){

//        RecommendationArguments arguments = new RecommendationArguments();

//        arguments.spotify_api = Server.spotify_api;
//        arguments.limit = 10;

//        arguments.market = CountryCode.US;
//        arguments.min_popularity = 10;
//        arguments.max_popularity = 20;
//        arguments.target_popularity = 25;

//        arguments.seed_genres = "hip-hop";
//        arguments.seed_artists = "15iVAtD3s3FsQR4w1v6M0P";
//        arguments.seed_tracks = "3WcC6NH9J77xPEvj1SOL7z";
//        arguments.min_tempo = 100;
//        arguments.max_tempo = 140;
//        arguments.target_tempo = 120;

//        arguments.min_energy = .4f;
//        arguments.max_energy = .6f;
//        arguments.target_energy = .5f;

         assertDoesNotThrow(() -> {
            RecommendationArguments args = new RecommendationArguments(Server.spotify_api, 10, "hip-hop", 100, 150, 120);
            final Recommendations recommendations = BrowsingUtilities.getRecommendations(args);

            System.out.println(recommendations.toString());
            TrackSimplified[] tracks = recommendations.getTracks();

            System.out.println(tracks.length);

            for(TrackSimplified track: tracks){
                System.out.println(track.getId());
            }
        });
    }
}
