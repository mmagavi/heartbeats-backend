package SpotifyUtilities;


import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import PlaylistGenerating.HeartRateRange;
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
public class RecommendationArguments {
    public SpotifyApi spotify_api;
    public int limit;
    public String seed_genres = "";
    public String seed_artists = "";
    public String seed_tracks = "";
    public float min_tempo = 0;
    public float max_tempo = 100;
    public float target_tempo = 50;


    public RecommendationArguments(){}
    public RecommendationArguments(SpotifyApi spotify_api, int limit, String genre,
                                   float min_tempo, float max_tempo, float target_tempo) throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException {
        this.spotify_api = spotify_api;
        this.limit = limit;
        this.seed_genres = genre;
        this.min_tempo = min_tempo;
        this.max_tempo = max_tempo;
        this.target_tempo = target_tempo;

        int genre_count = this.countGenres(this.seed_genres);

        StringBuilder stringTracks = new StringBuilder();
        int tracks_count = (5 - genre_count) / 2;
        if (tracks_count > 0){
            Track[] topTracks = PersonalizationUtilities.GetUsersTopTracks(this.spotify_api, tracks_count);
            for (Track track : topTracks){
                stringTracks.append(",");
                stringTracks.append(track.getId());
                System.out.println(track.getId());
            }
            if (!stringTracks.isEmpty()) {
                this.seed_tracks = stringTracks.substring(1);
            }
        }


        StringBuilder stringArtists = new StringBuilder();
        int artist_count = 5 - genre_count - tracks_count;
        if (artist_count > 0){
            Artist[] topArtists = PersonalizationUtilities.GetUsersTopArtists(this.spotify_api, artist_count);
            for (Artist artist : topArtists) {
                stringArtists.append(",");
                stringArtists.append(artist.getId());
                System.out.println(artist.getId());
            }
            if (!stringArtists.isEmpty()){
                this.seed_artists = stringArtists.substring(1);
            }
        }

    }

    public RecommendationArguments(SpotifyApi spotify_api, int limit, String genre,
                                   HeartRateRange range) throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException {
        this(spotify_api, limit, genre, range.min_heart_rate(),
                range.max_heart_rate(), range.target_heart_rate());
    }

    private int countGenres(String genres){
        int count = 0;
        while (genres.indexOf(',', count) != -1){
            count++;
        }
        return count+1;
    }

}
