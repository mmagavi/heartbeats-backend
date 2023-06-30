package PlaylistGenerating.PlaylistTypes.Interval;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import PlaylistGenerating.PlaylistTypes.GeneratePlaylist;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import static PlaylistGenerating.PlaylistTypes.CommonUtilities.getTrackIDs;
import static SpotifyUtilities.PersonalizationUtilities.GetUsersTopArtists;
import static SpotifyUtilities.PersonalizationUtilities.GetUsersTopTracks;
import static SpotifyUtilities.UserProfileUtilities.getCurrentUsersProfile;

/**
 * This class is used to generate a playlist for an interval style workout
 *
 * Plan:
 * First, determine the number of intervals we need:
 * 20 minute playlist -> 5-6 intervals?
 * 180 minute playlist -> ~12 intervals?
 * OR ask user how many intervals they want
 * OR set at 10 intervals? >60min
 * OR do by song length?
 *
 * <30 min -> do by num songs we can fit in
 *
 * Next, determine the length of each interval??
 * try combos?
 *
 * Next, pull songs at resting BPM and at target BPM
 * Two groups of songs, one for resting and one for target
 *
 *
 */
public class GenerateIntervalOne extends GeneratePlaylist {

    private final String seed_artists;
    private final String seed_tracks;
    private final int target_bpm;
    private final int seed_genres_provided;
    private final int num_intervals;

    private int desired_num_seed_artists;
    private int desired_num_seed_tracks;

    /**
     * Constructor for generating a classic style playlist
     * @param spotify_api SpotifyApi object that has been built with the current user's access token
     * @param genres Desired Genres
     * @param age Age of the user
     * @param workout_length Length of the workout
     */
    public GenerateIntervalOne(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity) throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException {

        super(spotify_api, genres, age, workout_length, intensity);

        target_bpm = getTargetBPM();

        num_intervals = getNumIntervals();

        seed_genres_provided = (int) genres.chars().filter(ch -> ch == ',').count();
        determineSeedLimits();

        seed_artists = getSeedArtists();
        seed_tracks = getSeedTracks();
    }

    /**
     * Determine the number of intervals we need based on the length of the workout
     * @return number of intervals we need
     */
    private int getNumIntervals() {
        // TODO: test & adjust this function
        // currently truncating anything after the decimal point... should we round up?

        if (workout_length_min <= 30) {
            // if workout is less than 30 minutes, do 1-song intervals
            return (int) ((workout_length_min - avg_song_len) / avg_song_len);
        } else if (workout_length_min <= 60) {
            // if workout is less than 60 minutes, do 2-song intervals
            return (int) ((workout_length_min - (2 * avg_song_len)) / (2*avg_song_len));
        } else if (workout_length_min <= 120) {
            // if workout is more than 60 minutes, do 3-song intervals
            return (int) ((workout_length_min - (3 * avg_song_len)) / (3*avg_song_len));
        } else {
            // if workout is more than 100 minutes, do 4-song intervals
            return (int) ((workout_length_min - (4 * avg_song_len)) / (4*avg_song_len));
        }
    }

    @Override
    public String generatePlaylist() throws Exception {

        User user = getCurrentUsersProfile(spotify_api);

        // get cool tracks
        String[] cool_tracks = findCoolTracks();
        // get warm tracks
        String[] warm_tracks = findWarmTracks();

        // concat tracks
        String[] playlist_track_uris = concatTracks(cool_tracks, warm_tracks);

        // return playlist
        return null;
    }

    @Override
    protected int getTargetBPM() {
        return 0;
    }

    /**
     * Based on the number of seed genres provided set the limits for seed artists and seed tracks.
     * This is important as the recommendation endpoint only allows 5 seed values in any combination
     * of genres, tracks, and artists
     */
    private void determineSeedLimits() {
        switch (seed_genres_provided) {
            case 1 -> {
                desired_num_seed_artists = 2;
                desired_num_seed_tracks = 2;
            }
            case 2 -> {
                desired_num_seed_artists = 1;
                desired_num_seed_tracks = 2;
            }
            case 3 -> {
                desired_num_seed_artists = 1;
                desired_num_seed_tracks = 1;
            }
        }
    }

    /**
     * Gets the users top artist(s) and returns a comma seperated string of their IDS
     *
     * @return comma seperated string of IDs of the users top artist(s)
     * @throws GetUsersTopArtistsRequestException if an error occurs fetching the users top artists
     */
    private String getSeedArtists() throws GetUsersTopArtistsRequestException {

        Artist[] seed_artists = GetUsersTopArtists(spotify_api, desired_num_seed_artists);

        StringBuilder string_builder = new StringBuilder();

        // Will flip to true when there is more than one artist is seed_artists, so they can be comma seperated
        boolean flag = false;

        for (Artist artist : seed_artists) {

            // This will be false for the first iteration preventing a leading comma but true for all the rest
            if (flag) {
                string_builder.append(",");
            }

            string_builder.append(artist.getId());

            flag = true;
        }

        return string_builder.toString();
    }

    /**
     * Gets the users top track(s) and returns a comma seperated string of their IDS
     *
     * @return comma seperated string of IDs of the users top track(s)
     * @throws GetUsersTopTracksRequestException if an error occurs fetching the users top tracks
     */
    private String getSeedTracks() throws GetUsersTopTracksRequestException {

        Track[] seed_tracks = GetUsersTopTracks(spotify_api, desired_num_seed_tracks);

        StringBuilder string_builder = new StringBuilder();

        // Will flip to true when there is more than one artist is seed_artists, so they can be comma seperated
        boolean flag = false;

        for (Track track : seed_tracks) {

            // This will be false for the first iteration preventing a leading comma but true for all the rest
            if (flag) {
                string_builder.append(",");
            }

            string_builder.append(track.getId());

            flag = true;
        }

        return string_builder.toString();
    }

    /**
     * Finds tracks for the 'cool' regions of the playlist
     * @return array of track IDs
     */
    private String[] findCoolTracks() throws GetRecommendationsException {

        String[] track_ids = null;

        // figure out how many tracks we need
        int num_tracks = (num_intervals - 1)/2 + 1;

        // determine the range of BPMs we want.. tbd
        int local_offset = 5; //todo: figure out how to determine this

        do {

            TrackSimplified[] recommended_tracks = getSortedRecommendations(num_tracks * 2,
                    resting_bpm - local_offset, resting_bpm + local_offset, resting_bpm);

            if (recommended_tracks != null) return getTrackIDs(recommended_tracks);

            System.out.println("null");
            System.out.println(local_offset);

            local_offset++;

        } while (true);
    }

    /**
     * Finds tracks for the 'warm' regions of the playlist
     * @return array of track IDs
     */
    private String[] findWarmTracks() throws GetRecommendationsException {

        // figure out how many tracks we need
        int num_tracks = (num_intervals - 1)/2;

        // determine the range of BPMs we want.. tbd
        int local_offset = 5; //todo: figure out how to determine this

        do {

            TrackSimplified[] recommended_tracks = getSortedRecommendations(num_tracks * 2,
                    target_bpm - local_offset, target_bpm + local_offset, target_bpm);

            if (recommended_tracks != null) return getTrackIDs(recommended_tracks);

            System.out.println("null");
            System.out.println(local_offset);

            local_offset++;

        } while (true);
    }

    /**
     * Concatenates the cool and warm tracks into a single array of the proper length
     * @param cool_tracks tracks for the cool regions of the playlist
     * @param warm_tracks tracks for the warm regions of the playlist
     * @return array of track IDs
     */
    private String[] concatTracks(String[] cool_tracks, String[] warm_tracks) {

        

        return null;
    }
}