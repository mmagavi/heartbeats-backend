package PlaylistGenerating.PlaylistTypes;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.TrackExceptions.GetAudioFeaturesForSeveralTracksException;
import ExceptionClasses.TrackExceptions.GetAudioFeaturesForTrackException;
import SpotifyUtilities.RecommendationArguments;
import SpotifyUtilities.TrackUtilities;
import com.neovisionaries.i18n.CountryCode;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

import static SpotifyUtilities.BrowsingUtilities.getRecommendations;
import static SpotifyUtilities.TrackUtilities.*;

public class CommonUtilities {

    /**
     * Loops through all the given tracks and stores their URIs in a string array which is then returned
     *
     * @param tracks array of tracks to fetch the URI from
     * @return String array of all the given track's URIs, returns null if tracks is null
     */
    public static String[] getTrackURIs(TrackSimplified[] tracks) {

        if (tracks == null) return null;

        int size = tracks.length;

        String[] uris = new String[size];

        for (int index = 0; index < size; index++) {
            uris[index] = tracks[index].getUri();
        }
        return uris;
    }

    /**
     * Loops through all the given tracks and stores their IDs in a string array which is then returned
     *
     * @param tracks array of tracks to fetch the ID from
     * @return String array of all the given track's IDs, returns null if tracks is null
     */
    public static String[] getTrackIDs(TrackSimplified[] tracks) {

        if (tracks == null) return null;

        int size = tracks.length;

        String[] ids = new String[size];

        for (int index = 0; index < size; index++) {
            ids[index] = tracks[index].getId();
        }
        return ids;
    }

    /**
     * Loops through all the given features and stores their URIs in a string array which is then returned
     *
     * @param features array of features to fetch the URI from
     * @return String array of all the given track's URIs, returns null if features is null
     */
    public static String[] getTrackURIsFromAudioFeatures(AudioFeatures[] features) {

        if (features == null) return null;

        int size = features.length;

        String[] uris = new String[size];

        for (int index = 0; index < size; index++) {
            uris[index] = features[index].getUri();
        }
        return uris;
    }

    /**
     * Adds all the elements in the provided array into the provided array list and finally returning the array list
     *
     * @param array_list array list to be added to
     * @param array array whose elements will be added to the provided array_list
     * @return ArrayList<TrackSimplified> with the elements of array added to it, null if either argument is null
     */
    public static ArrayList<TrackSimplified> addAll(ArrayList<TrackSimplified> array_list, TrackSimplified[] array){

        if(array_list == null || array == null) return new ArrayList<TrackSimplified>();

        TrackSimplified current_track;

        array_list.addAll(Arrays.asList(array));

        return array_list;
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

    /**
     * Recursively searches for the greatest common factor GCF between the two given numbers
     * @param num_one first number
     * @param num_two second number
     * @return the GCF of num_one and num_two
     */
    public static int GCF(int num_one, int num_two){

        if(num_two == 0){
            return num_one;
        } else{
            return GCF(num_two, num_one % num_two);
        }

    }

    /**
     * Prints out the tempo of each track provided in the tracks array
     *
     * @param spotify_api api object used to make api calls
     * @param tracks TrackSimplified array from which to print the tempo of each song
     * @throws GetAudioFeaturesForSeveralTracksException If an error is encountered with Spotify's API
     */
    public static void printTempos(SpotifyApi spotify_api, TrackSimplified[] tracks)
            throws GetAudioFeaturesForSeveralTracksException {

        String[] track_ids = getTrackIDs(tracks);
        AudioFeatures[] features = getAudioFeaturesForSeveralTracks(spotify_api, track_ids);

        for(AudioFeatures feature: features){
            System.out.println(feature.getTempo());
        }
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
                replacement_map.put(track, index); // We know we have a dupe/non-playable so it needs replacing
            }
        }

        for (TrackSimplified duplicate_track : replacement_map.keySet()) {
            //Track track = TrackUtilities.getTrack(spotify_api, uri);
            TrackSimplified replacement =
                    replaceTrack(spotify_api, track_map, duplicate_track, genres, seed_artists, seed_tracks, market);

            int track_index = replacement_map.get(duplicate_track); // The index we need to put the new non-duplicate song into

            tracks[track_index] = replacement; // Replace the song
            track_map.put(replacement, track_index); // make sure to update the track map so we dont duplicate with replacments
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
            if(recommended_tracks.length < limit){
                offset++;
                continue;
            }

            Arrays.sort(recommended_tracks, duration_comparator);

            TrackSimplified closest_track = null;

            for (TrackSimplified current_track : recommended_tracks) {

                boolean is_playable = current_track.getIsPlayable();

                // If the track we are considering is a duplicate or not playable continue to the next candidate
                if (track_map.get(current_track) != null || !is_playable) continue;

                // AudioFeatures current_features = getAudioFeaturesForTrack(spotify_api, current_track.getId());

                // Compare the durations of the closest track and the current, replacing the closest track with the
                // current if the current track is closer to the desired duration
                closest_track = getTrackWithClosestDuration(target_duration_ms, closest_track, current_track);

            }

            if(isGoodDuration(target_duration_ms, closest_track, margin_of_error)) return closest_track;

            offset++; // relax constraints
            margin_of_error += .01;
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

        if(track1 == null) return track2; // expected on first run

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
