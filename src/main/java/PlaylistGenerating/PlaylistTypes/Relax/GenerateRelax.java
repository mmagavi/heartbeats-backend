package PlaylistGenerating.PlaylistTypes.Relax;

import ExceptionClasses.ArtistExceptions.GetSeveralArtistsException;
import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import ExceptionClasses.TrackExceptions.GetAudioFeaturesForSeveralTracksException;
import PlaylistGenerating.PlaylistTypes.GeneratePlaylist;
import SpotifyUtilities.PlaylistUtilities;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.*;

import static PlaylistGenerating.PlaylistTypes.CommonUtilities.*;
import static PlaylistGenerating.PlaylistTypes.Relax.RelaxCheckingUtilities.checkIntervalDuration;
import static SpotifyUtilities.PlaylistUtilities.createPlaylist;
import static SpotifyUtilities.TrackUtilities.getAudioFeaturesForSeveralTracks;
import static SpotifyUtilities.TrackUtilities.tempo_comparator;

public class GenerateRelax extends GeneratePlaylist {

    private final int num_intervals;
    protected static int target_len_ms;
    protected static int min_target_len_ms;
    protected static int max_target_len_ms;
    private final int limit = 21; //TODO: find out if this limit should be dynamic like the interval ones
    protected static int interval_len_ms;
    protected static int min_interval_len_ms;
    protected static int max_interval_len_ms;

    private static HashMap<String, Integer> selected_songs;


    public GenerateRelax(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity,
                         boolean is_personalized) throws GetUsersTopArtistsRequestException,
            GetUsersTopTracksRequestException, GetCurrentUsersProfileException, GetSeveralArtistsException {
        super(spotify_api, genres, age, workout_length, intensity, is_personalized);

        num_intervals = findNumIntervals();
        tracks_per_interval = Math.round(((float) workout_length / (float) num_intervals) / avg_song_len);

        target_len_ms = workout_len_ms;
        interval_len_ms = workout_len_ms / num_intervals;

        setWorkoutLengths(margin_of_error);
        setIntervalLengths(margin_of_error);

        selected_songs = new HashMap<>();
    }

    @Override
    public String generatePlaylist() throws Exception {

        TrackSimplified[] tracks = findTracks();

        eliminateDupesAndNonPlayable(spotify_api, tracks, genres, seed_artists, seed_tracks, user.getCountry());

        String[] track_ids = getTrackIDs(tracks);

        // Sort the tracks in descending tempo order to make sure all tracks are in the right order
        AudioFeatures[] features = getAudioFeaturesForSeveralTracks(spotify_api, track_ids);
        Arrays.sort(features, tempo_comparator);

//        System.out.println("target: " + target_bpm);
//
//        for(AudioFeatures feature: features){
//            System.out.println(feature.getTempo());
//        }

        String[] playlist_track_uris = getTrackURIsFromAudioFeatures(features);

        // Create a playlist on the user's account
        Playlist playlist = createPlaylist(spotify_api, user.getId(), user.getDisplayName());
        String playlist_id = playlist.getId();

        System.out.println("Creating Relax Playlist");
        PlaylistUtilities.addItemsToPlaylist(spotify_api, playlist_id, playlist_track_uris);

        return playlist_id;
    }

    private TrackSimplified[] findTracks() throws GetAudioFeaturesForSeveralTracksException,
            GetRecommendationsException {

        ArrayList<TrackSimplified> track_list = new ArrayList<>();

        // difference between target bpm for each interval
        int bpm_difference = ((target_bpm - resting_bpm) / num_intervals);
        int local_max_bpm = target_bpm;
        int local_min_bpm = target_bpm - bpm_difference;
        // local target is halfway between local max and min
        int local_target_bpm = local_max_bpm - ((local_max_bpm - local_min_bpm) / 2);

        TrackSimplified[] tracks_to_add;
        float local_moe; // Keeps track of moe for duration purposes which we will be altering here

        for (int interval = 0; interval < num_intervals; interval++) {

            local_moe = margin_of_error;

            do {
                TrackSimplified[] recommended_tracks = getRecommendedTracks(local_min_bpm, local_max_bpm, local_target_bpm);

                tracks_to_add = getIntervalTracks(recommended_tracks);

                setIntervalLengths(local_moe += .01);

            } while (tracks_to_add == null);

            setIntervalLengths(margin_of_error); // restore moe
            track_list = addAll(track_list, tracks_to_add); // add the new tracks to the track list

            // Update bpm targets for next interval
            local_min_bpm -= bpm_difference;
            local_max_bpm -= bpm_difference;
            local_target_bpm -= bpm_difference;
        }

        TrackSimplified[] tracks = track_list.toArray(TrackSimplified[]::new);

        return tracks;
    }

    private TrackSimplified[] getIntervalTracks(TrackSimplified[] tracks){

        Deque<TrackSimplified> deque = new ArrayDeque<>();

        int index = 0;
        DURATION_RESULT result;
        TrackSimplified current_track;

        // Get the first batch of songs into the deque
        for (; index < tracks_per_interval; index++) {
            current_track = tracks[index];
            deque.add(current_track);
        }

        result = checkIntervalDuration(deque);

        if (result == DURATION_RESULT.ACCEPTABLE) return deque.toArray(TrackSimplified[]::new);
        // If the shortest combination of tracks is too long there is no suitable combination so return null
        if (result == DURATION_RESULT.TOO_LONG) return null;

        for (; index < tracks.length; index++) {

            current_track = tracks[index];

            // This essentially shifts the group of tracks we are analyzing to the right (longer) side
            deque.removeFirst(); // remove the shortest track
            deque.add(current_track); // add the next track in line

            result = checkIntervalDuration(deque);

            if (result == DURATION_RESULT.ACCEPTABLE) return deque.toArray(TrackSimplified[]::new);

            if (result == DURATION_RESULT.TOO_LONG) return null; // If now too long there is no acceptable combination
        }

        return null;
    }

    /**
     * Checks if the provided song is a duplicate (if it is already present in the selected song's hashmap)
     *
     * @param track TrackSimplified object to determine if it is a duplicate
     * @return true if it is a duplicate, false otherwise
     */
    private boolean isDuplicate(TrackSimplified track){
        String uri = track.getUri();

        // If the query returns null then it is not in the selected songs map
        if(selected_songs.get(uri) == null) return false;

        return true;
    }

    /**
     * Queries the Spotify recommendation endpoint with protections to ensure the correct number of tracks are returned
     *
     * @param min_bpm    minimum bpm for recommendation endpoint query
     * @param max_bpm    maximum bpm for recommendation endpoint query
     * @param target_bpm target bpm for recommendation endpoint query
     * @return TrackSimplified array of tracks from the recommendation endpoint
     * @throws GetRecommendationsException If an error was encountered in the recommendation endpoint
     */
    private TrackSimplified[] getRecommendedTracks(int min_bpm, int max_bpm, int target_bpm)
            throws GetRecommendationsException {

        TrackSimplified[] recommended_tracks;
        int local_offset = 0;

        do {

            recommended_tracks = getSortedRecommendations(limit, min_bpm - local_offset,
                    max_bpm + local_offset, target_bpm);

            local_offset++; // increase offset to find more tracks as the current bpm boundaries may be too restrictive

        } while (recommended_tracks.length < limit);

        return recommended_tracks;
    }

    /**
     * Determines the number of intervals for the playlist based on workout length
     *
     * @return number of intervals to be made for the playlist
     */
    private int findNumIntervals() {
        // Use only 3 intervals for the shorter playlists
        if (workout_length_min <= 50) {
            return 3;
        } else {
            // Try and split things into intervals of 5 songs each
            return Math.round(workout_length_min / (avg_song_len * 5));
        }
    }

    /**
     * Sets the min and max workout lengths based on the provided margin of error
     *
     * @param margin_of_error margin of error used in setting the min and max workout lengths
     */
    private void setWorkoutLengths(float margin_of_error) {

        // MilliSeconds of length that is acceptable for the warmup / wind-down sequence based on our MOE
        min_target_len_ms = target_len_ms - (int) (target_len_ms * margin_of_error);
        max_target_len_ms = target_len_ms + (int) (target_len_ms * margin_of_error);
    }

    /**
     * Sets the min and max interval lengths based on the provided margin of error
     *
     * @param margin_of_error margin of error used in setting the min and max interval lengths
     */
    private void setIntervalLengths(float margin_of_error) {

        // MilliSeconds of length that is acceptable for the warmup / wind-down sequence based on our MOE
        min_interval_len_ms = interval_len_ms - (int) (interval_len_ms * margin_of_error);
        max_interval_len_ms = interval_len_ms + (int) (interval_len_ms * margin_of_error);
    }
}
