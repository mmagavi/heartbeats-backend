package PlaylistGenerating.PlaylistTypes;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.PlaylistExceptions.AddItemsToPlaylistException;
import ExceptionClasses.PlaylistExceptions.CreatePlaylistException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import PlaylistGenerating.HeartRateRange;
import SpotifyUtilities.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.*;


abstract class GeneratePlaylist {
    /**
     * number of songs in playlist (estimated, given avg song length is 3.5 mins)
     */
    protected final int numSongs;
    /**
     * genre_seed user enters from front-end
     */
    protected final String genre;
    /**
     * the spotifyAPI containing the user's information
     */
    protected final SpotifyApi spotifyApi;

    /**
     * Hashmap going from a HeartRateRange to an integer.
     * Represents the BPM and how many songs we need at that BPM to get recommendations for from spotify
     *
     * Example:
     *   params: 10 song playlist, quickStart, !quickEnd, rest = 60 bpm, peak = 120 bpm
     *      for start, we need songs at:     - 60, 90 bpm
     *      for end, we need songs at:       - 105, 90, 75, 60 bpm
     *
     *   thus, songsPerInterval looks like
     *      [60]    -> 2
     *      [75]    -> 1
     *      [90]    -> 2
     *      [105]   -> 1
     *      [120]   -> 4
     */
    protected Map<HeartRateRange, Integer> songsPerInterval;

    /**
     * HashMap going from a HeartRateRange to a LinkedList of spotify Track URIs (songs basically)
     * Stores the songs that we call getRecommendations for
     * The size of the linked list is related to the value for the key in `songsPerInterval`
     */
    protected Map<HeartRateRange, LinkedList<String>> songsToChoose;

    /**
     * The final outputted playlist's ID
     */
    protected String playlistID;
    
    public GeneratePlaylist(int lenPlaylist, String genre, SpotifyApi spotifyApi) throws GetCurrentUsersProfileException {
        this.numSongs = (int) (lenPlaylist / 3.5);
        this.genre = genre;
        this.songsPerInterval = new HashMap<>();
        this.songsToChoose = new HashMap<>();
        this.spotifyApi = spotifyApi;
        this.playlistID = null;
    }

    /**
     * Fetches and returns the user from this.spotifyAPI
     * @return the current user as a User class
     * @throws GetCurrentUsersProfileException
     */
    private User instantiateUser() throws GetCurrentUsersProfileException {
        return UserProfileUtilities.getCurrentUsersProfile(this.spotifyApi);
    }

    /**
     * increments the inputted value in songsPerInterval correctly
     * @param targetBPM - the target to increase in songsPerInterval by 1
     */
    protected void addCountToInterval(HeartRateRange targetBPM) {
        if (!this.songsPerInterval.containsKey(targetBPM)) {
            this.songsPerInterval.put(targetBPM, 1);
        } else {
            this.songsPerInterval.put(targetBPM, this.songsPerInterval.get(targetBPM) + 1);
        }
    }

    /**
     * MUST BE CALLED in the constructor prior to any attempt to create the outputted playlist
     * Populates `songsToChoose` based on `songsPerInterval`
     * Real about it more in the javadoc for the fields `songsPerInterval` and `songsToChoose`
     * @throws GetRecommendationsException if there's an issue with the getRecommendations call
     */
    protected void populateSongsToChoose() throws GetRecommendationsException, GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException {
        for (HeartRateRange bpm : this.songsPerInterval.keySet()){
            int numSongs = this.songsPerInterval.get(bpm);
            if (numSongs <= 0) {
                this.songsToChoose.put(bpm, new LinkedList<>());
                continue;
            }
            RecommendationArguments args = new RecommendationArguments(this.spotifyApi, numSongs, this.genre, bpm);
            Recommendations rec = BrowsingUtilities.getRecommendations(args);
            if (rec == null) {
                this.songsToChoose.put(bpm, new LinkedList<>());
                continue;
            }
            TrackSimplified[] tracks = rec.getTracks();

            LinkedList<String> songs = new LinkedList<>();
            for (TrackSimplified track : tracks) {
                songs.addLast(track.getUri());
            }
            this.songsToChoose.put(bpm, songs);
        }
    }

    /**
     * Fetches the spotifyAPI to create a new playlist in the current user's profile
     * @throws CreatePlaylistException
     * @throws GetCurrentUsersProfileException
     */
    protected void createPlaylist() throws CreatePlaylistException, GetCurrentUsersProfileException {
        User user = this.instantiateUser();
        this.playlistID = PlaylistUtilities.createPlaylist(this.spotifyApi, user.getId(), user.getDisplayName()).getId();
    }

    /**
     * Adds a song or an array of songs to the playlist created
     * @param uris - String[] of the uris of the songs
     * @throws AddItemsToPlaylistException - if there is an issue in adding the songs
     */
    protected void addToPlaylist(String[] uris) throws AddItemsToPlaylistException {
        PlaylistUtilities.addItemsToPlaylist(this.spotifyApi, this.playlistID, uris);
    }

    /**
     * Adds a song or an array of songs to the playlist created
     * @param bpm - bpm of song needed to be added
     * @throws AddItemsToPlaylistException - if there is an issue in adding the song
     */
    protected void addToPlaylist(HeartRateRange bpm, int numSongs, Map<HeartRateRange, LinkedList<String>> songs) throws AddItemsToPlaylistException {
        if (!songs.containsKey(bpm)){
            return;
        }
        if (numSongs == 0) return;
        int nSongs = Math.min(numSongs, songs.get(bpm).size());
        String[] wrap = new String[nSongs];
        for (int i = 0; i < nSongs; i++) {
            wrap[i] = songs.get(bpm).pollFirst();
        }
        this.addToPlaylist(wrap);
    }


    /**
     * Creates a playlist based on the user's liked and recommended songs for their chosen activiity.
     * @return the string id of the playlist that was created.
     */
    public String getPlaylist() {
        String retID = this.playlistID;
        return retID;
    }
}
