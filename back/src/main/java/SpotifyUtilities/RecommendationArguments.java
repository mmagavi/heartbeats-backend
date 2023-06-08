package SpotifyUtilities;


import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import PlaylistGenerating.TargetHeartRateRange;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Track;


/**
 * {@link #spotify_api} spotify_api:       SpotifyApi object that has been built with the current user's access token<p>
 * {@link #limit} limit:                      Number of songs to fetch (1 - 100)<p>
 * {@link #min_tempo} min_tempo:               min tempo desired<p>
 * {@link #max_tempo} max_tempo:               max tempo desired<p>
 * {@link #target_tempo} target_tempo:         exact tempo desired<p>
 */
public record RecommendationArguments(
    SpotifyApi spotify_api,
    int limit,
    String seed_genres,
    String seed_artists,
    String seed_tracks,
    float min_tempo,
    float max_tempo,
    float target_tempo){}
