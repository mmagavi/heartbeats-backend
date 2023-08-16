package SpotifyUtilities;

import com.neovisionaries.i18n.CountryCode;
import se.michaelthelin.spotify.SpotifyApi;

public record GenreRecommendationArguments(
        SpotifyApi spotify_api,
        int limit,
        String seed_genres,
        float min_tempo,
        float max_tempo,
        float target_tempo,
        CountryCode market) {
}
