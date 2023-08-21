package SpotifyUtilities;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import org.apache.hc.core5.http.ParseException;
import org.eclipse.jetty.websocket.common.io.http.HttpResponseHeaderParser;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.io.IOException;

public class BrowsingUtilities {

    /**
     * Gets recommendations using Spotify's recommendation endpoint
     *
     * @return Recommendations object with the fetched tracks
     * @throws GetRecommendationsException if exception encountered in getRecommendationsRequest.execute()
     */
    public static Recommendations getRecommendations(RecommendationArguments args)
            throws GetRecommendationsException {

        try {

            String seed_artists = args.seed_artists();
            String seed_tracks = args.seed_tracks();
            String seed_genres = args.seed_genres();

            System.out.println("seed artists: " + args.seed_artists());
            System.out.println("seed tracks: " + args.seed_tracks());
            System.out.println("seed genres: " + args.seed_genres());

            GetRecommendationsRequest getRecommendationsRequest =
                    args.spotify_api().getRecommendations()
                            .limit(args.limit())
                            .min_tempo(args.min_tempo())
                            .max_tempo(args.max_tempo())
                            .target_tempo(args.target_tempo())
                            .seed_genres(args.seed_genres())
                            .seed_tracks(args.seed_tracks())
                            .seed_artists(args.seed_artists())
                            .market(args.market())
                            .build();

            return getRecommendationsRequest.execute();

        } catch (IOException | SpotifyWebApiException | ParseException ex) {

            throw new GetRecommendationsException(ex.getMessage());
        }
    }

    /**
     * Gets recommendations using Spotify's recommendation endpoint
     *
     * @return Recommendations object with the fetched tracks
     * @throws GetRecommendationsException if exception encountered in getRecommendationsRequest.execute()
     */
    public static Recommendations getGenreRecommendations(RecommendationArguments args)
            throws GetRecommendationsException {

        try {
            GetRecommendationsRequest getRecommendationsRequest =
                    args.spotify_api().getRecommendations()
                            .limit(args.limit())
                            .seed_genres(args.seed_genres())
                            .min_tempo(args.min_tempo())
                            .max_tempo(args.max_tempo())
                            .target_tempo(args.target_tempo())
                            .market(args.market())
                            .build();

            return getRecommendationsRequest.execute();

        } catch (IOException | SpotifyWebApiException | ParseException ex) {

            throw new GetRecommendationsException(ex.getMessage());
        }
    }

    //TODO: Overload this method in case we do not always want to use all these arguments :)

    /**
     * Gets recommendations using Spotify's recommendation endpoint
     *
     * @return Recommendations object with the fetched tracks
     * @throws GetRecommendationsException if exception encountered in getRecommendationsRequest.execute()
     */
    public static Recommendations getRecommendationTempoRange(RecommendationArguments args)
            throws GetRecommendationsException {

        try {
            GetRecommendationsRequest getRecommendationsRequest =
                    args.spotify_api().getRecommendations()
                            .limit(args.limit())
                            .min_tempo(args.min_tempo())
                            .max_tempo(args.max_tempo())
                            .seed_genres(args.seed_genres())
                            .seed_tracks(args.seed_tracks())
                            .seed_artists(args.seed_artists())
                            .market(args.market())
                            .build();

            return getRecommendationsRequest.execute();

        } catch (IOException | SpotifyWebApiException | ParseException ex) {

            throw new GetRecommendationsException(ex.getMessage());
        }
    }
}