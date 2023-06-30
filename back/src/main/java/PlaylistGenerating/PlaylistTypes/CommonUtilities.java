package PlaylistGenerating.PlaylistTypes;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.TrackExceptions.GetAudioFeaturesForTrackException;
import SpotifyUtilities.RecommendationArguments;
import SpotifyUtilities.TrackUtilities;
import com.neovisionaries.i18n.CountryCode;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

import static SpotifyUtilities.BrowsingUtilities.getRecommendations;
import static SpotifyUtilities.TrackUtilities.duration_comparator;
import static SpotifyUtilities.TrackUtilities.getAudioFeaturesForTrack;

public class CommonUtilities {

    /**
     * Loops through all the given tracks and stores their URIs in a string array which is then returned
     *
     * @param tracks array of tracks to fetch the id from
     * @return String array of all the given track's ids, returns null if tracks is null
     */
    public static String[] getTrackURIs(TrackSimplified[] tracks) {

        if (tracks == null) return null;

        int size = tracks.length;

        String[] ids = new String[size];

        for (int index = 0; index < size; index++) {
            ids[index] = tracks[index].getUri();
            //ids[index] = tracks[index].getId();
        }
        return ids;
    }

    /**
     * @param arrays array arguments to concat
     * @return String array of the concatenated arguments
     */
    public static TrackSimplified[] concatTracks(TrackSimplified[]... arrays) {

        Stream<TrackSimplified> stream = Stream.of();

        for (TrackSimplified[] array : arrays) {
            stream = Stream.concat(stream, Arrays.stream(array));
        }

        return stream.toArray(TrackSimplified[]::new);
    }

    public static TrackSimplified[] eliminateDupesAndNonPlayable(SpotifyApi spotify_api, TrackSimplified[] tracks,
                                                                 String genres, String seed_artists, String seed_tracks,
                                                                 CountryCode market)
            throws GetAudioFeaturesForTrackException, GetRecommendationsException {

        HashMap<TrackSimplified, Integer> track_map = new HashMap<>();
        // Store all the tracks that need replacement here
        HashMap<TrackSimplified, Integer> replacement_map = new HashMap<>();

        // Find which tracks are duplicates and place them in our duplicate map
        for (int index = 0; index < tracks.length; index++) {

            TrackSimplified track = tracks[index];
            boolean is_playable = track.getIsPlayable();

            // If the track is not already in the map it will return null from the .get() method
            if (track_map.get(track) == null && is_playable) {
                track_map.put(track, index);
            } else {
                replacement_map.put(track, index); // If null was not returned above we know we have a dupe/non-playable
            }
        }

        for (TrackSimplified duplicate_track : replacement_map.keySet()) {
            //Track track = TrackUtilities.getTrack(spotify_api, uri);
            TrackSimplified replacement =
                    replaceTrack(spotify_api, track_map, duplicate_track, genres, seed_artists, seed_tracks, market);

            int track_index = replacement_map.get(duplicate_track); // The index we need to put the new non-duplicate song into

            tracks[track_index] = replacement; // Replace the song
        }
        return tracks;
    }

    private static TrackSimplified replaceTrack(SpotifyApi spotify_api, HashMap<TrackSimplified, Integer> track_map,
                                                TrackSimplified track, String genres, String seed_artists, String seed_tracks,
                                                CountryCode market)
            throws GetAudioFeaturesForTrackException, GetRecommendationsException {

        System.out.println("Replacing");

        AudioFeatures track_features = TrackUtilities.getAudioFeaturesForTrack(spotify_api, track.getId());
        float tempo = track_features.getTempo();
        int target_duration_ms = track_features.getDurationMs();
        int offset = 1;
        int limit = 21;
        float margin_of_error = .1f;

        while(true) {
            RecommendationArguments current_arguments = new RecommendationArguments(
                    spotify_api, limit, genres, seed_artists, seed_tracks,
                    tempo - offset, tempo + offset, tempo, market);

            Recommendations recommendations = getRecommendations(current_arguments);
            TrackSimplified[] recommended_tracks = recommendations.getTracks();

            // relax constraints
            if(recommended_tracks.length == 0){
                offset++;
                continue;
            }

            Arrays.sort(recommended_tracks, duration_comparator);

            TrackSimplified closest_track = recommended_tracks[0];

            for(int index = 1; index < recommended_tracks.length; index++){

                TrackSimplified current_track = recommended_tracks[index];
                boolean is_playable = current_track.getIsPlayable();

                // If the track we are considering is a duplicate or not playable continue to the next candidate
                if(track_map.get(current_track) != null || !is_playable) continue;

                AudioFeatures current_features = getAudioFeaturesForTrack(spotify_api, current_track.getId());

                // Compare the durations of the closest track and the current, replacing the closest track with the
                // current if the current track is closer to the desired duration
                closest_track = getTrackWithClosestDuration(target_duration_ms, closest_track, current_track);
            }

            if(isGoodDuration(target_duration_ms, closest_track, margin_of_error)) return closest_track;

            offset++; // relax constraints
            margin_of_error += .05;
        }
    }

    /**
     * Checks if the candidate has a duration within the limits set by the provided target duration and margin of error
     * @param target_duration duration which we want to be close to
     * @param candidate TrackSimplified object which is being checked
     * @param margin_of_error how far from the target duration we will allow
     * @return true of the candidate is within the allowable range, false otherwise
     */
    private static boolean isGoodDuration(int target_duration, TrackSimplified candidate, float margin_of_error){

        int candidate_duration = candidate.getDurationMs();

        int min_duration = target_duration - (int)(target_duration * margin_of_error);
        int max_duration = target_duration + (int)(target_duration * margin_of_error);

        return candidate_duration >= min_duration &&  candidate_duration <= max_duration;
    }

    /**
     * Compares two tracks and returns the track which has a closer duration to the provided duration
     * @param duration_ms duration to compare with track1 and track2 duration (milliseconds)
     * @param track1 first track
     * @param track2 second track
     * @return TrackSimplified object which has the closest duration to the provided duration, track1 if equal duration
     */
    private static TrackSimplified getTrackWithClosestDuration(int duration_ms, TrackSimplified track1, TrackSimplified track2){

        int track1_duration = track1.getDurationMs();
        int track2_duration = track2.getDurationMs();

        int difference_one = Math.abs(duration_ms - track1_duration);
        int difference_two = Math.abs(duration_ms - track2_duration);

        if(difference_one <= difference_two){
            return track1;
        } else {
            return track2;
        }
    }
}
