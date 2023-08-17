package PlaylistGenerating.PlaylistTypes.Interval;

import ExceptionClasses.ArtistExceptions.GetSeveralArtistsException;
import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import ExceptionClasses.TrackExceptions.GetAudioFeaturesForTrackException;
import SpotifyUtilities.PlaylistUtilities;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.*;

import static PlaylistGenerating.PlaylistTypes.CommonUtilities.*;
import static SpotifyUtilities.PlaylistUtilities.createPlaylist;
import static SpotifyUtilities.UserProfileUtilities.getCurrentUsersProfile;

/**
 * This class is used to generate a playlist for an interval style workout
 */
public class GenerateIntervalOne extends GenerateInterval {

    /**
     * Constructor for generating an interval one style playlist
     * Inherits from the superclass
     *
     * @param spotify_api    SpotifyApi object that has been built with the current user's access token
     * @param genres         Desired Genres
     * @param age            Age of the user
     * @param workout_length Length of the workout
     * @param intensity    Intensity of the workout
     */
    public GenerateIntervalOne(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity,
                               boolean is_personalized) throws GetUsersTopArtistsRequestException,
            GetUsersTopTracksRequestException, GetCurrentUsersProfileException, GetSeveralArtistsException {
        super(spotify_api, genres, age, workout_length, intensity, is_personalized);
    }

    @Override
    public String generatePlaylist() throws Exception {

        System.out.println("Generating Interval One Playlist");

        User user = getCurrentUsersProfile(spotify_api);

        // Build playlist (get & organize tracks)
        TrackSimplified[] final_playlist_tracks = buildPlaylist();

        eliminateDupesAndNonPlayable(spotify_api, final_playlist_tracks, genres,
                seed_artists, seed_tracks, user.getCountry());

        // Create a playlist on the user's account
        Playlist playlist = createPlaylist(spotify_api, user.getId(), user.getDisplayName());
        String playlist_id = playlist.getId();
        String[] playlist_track_uris = getTrackURIs(final_playlist_tracks);

        System.out.println("Creating Interval One Playlist");
        PlaylistUtilities.addItemsToPlaylist(spotify_api, playlist_id, playlist_track_uris);

        return playlist_id;
    }

    /**
     * Gets tracks and builds a playlist with the proper
     * varying tempo and intervals
     *
     * @return the final array of track IDS
     */
    private TrackSimplified[] buildPlaylist() throws GetRecommendationsException, GetAudioFeaturesForTrackException {

        ArrayList<TrackSimplified> recommended_slow_tracks;
        ArrayList<TrackSimplified> slow_intervals;

        ArrayList<TrackSimplified> recommended_fast_tracks;
        ArrayList<TrackSimplified> fast_intervals;

        TrackSimplified[] final_playlist = null;

        // Keeps track of moe for duration purposes which we will be altering here
        float local_moe = margin_of_error;

        do {

            //System.out.println("Getting Recommended Tracks");
            // Get recommended tracks
            recommended_slow_tracks = getRecommendedTracks(resting_bpm, query_limit);
            recommended_fast_tracks = getRecommendedTracks(target_bpm, query_limit);

            //System.out.println("Finding Rough Intervals");
            // Fill the intervals the best we can with the given 100 tracks
            slow_intervals = findRoughIntervals(recommended_slow_tracks, num_slow_intervals);
            fast_intervals = findRoughIntervals(recommended_fast_tracks, num_fast_intervals);

            // If enough of the intervals have been found, fill the gaps and sort them into one correctly ordered array
            if(slow_intervals != null && fast_intervals != null){

                //System.out.println("Filling Intervals");
                // Find a good ordering of each interval
                slow_intervals = fillIntervals(slow_intervals, num_slow_tracks, resting_bpm);
                fast_intervals = fillIntervals(fast_intervals, num_fast_tracks, resting_bpm);

                //System.out.println("Ordering Tracks");
                // Order the tracks correctly
                final_playlist = orderTracks(slow_intervals, fast_intervals);
            }

            setIntervalLengths(local_moe += .01); // loosen MOE

        } while (final_playlist == null);

        setIntervalLengths(margin_of_error); // restore moe

        return final_playlist;
    }
}