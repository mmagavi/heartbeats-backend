package PlaylistGenerating.PlaylistTypes;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.TrackExceptions.GetAudioFeaturesForTrackException;
import ExceptionClasses.TrackExceptions.GetTrackException;
import SpotifyUtilities.LibraryUtilities;
import SpotifyUtilities.RecommendationArguments;
import SpotifyUtilities.TrackUtilities;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

import static SpotifyUtilities.BrowsingUtilities.getRecommendations;
import static SpotifyUtilities.TrackUtilities.duration_comparator;
import static SpotifyUtilities.TrackUtilities.getAudioFeaturesForTrack;

public class CommonUtilities {

    //TODO rename to getTrackURIs

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

    public static String[] eliminateDuplicates(SpotifyApi spotify_api, String[] track_uris, String genres,
                                                  String seed_artists, String seed_tracks)
            throws GetAudioFeaturesForTrackException, GetRecommendationsException {

        HashMap<String, Integer> track_map = new HashMap<>();
        HashMap<String, Integer> duplicate_map = new HashMap<>();

        // Find which tracks are duplicates and place them in our duplicate map
        for (int index = 0; index < track_uris.length; index++) {

            String track_uri = track_uris[index];

            // If the track is not already in the map it will return null from the .get() method
            if (track_map.get(track_uri) == null) {
                track_map.put(track_uri, index);
            } else {
                duplicate_map.put(track_uri, index); // If null was not returned above we know we have a duplicate
            }
        }

        for (String uri : duplicate_map.keySet()) {
            //Track track = TrackUtilities.getTrack(spotify_api, uri);
            String replacement = replaceTrack(spotify_api, track_map, uri, genres, seed_artists, seed_tracks);

            int track_index = duplicate_map.get(uri); // The index we need to put the new non-duplicate song into

            track_uris[track_index] = replacement; // Replace the song
        }

        return track_uris;
    }

    private static String replaceTrack(SpotifyApi spotify_api, HashMap<String, Integer> track_map, String uri,
                                       String genres, String seed_artists, String seed_tracks)
            throws GetAudioFeaturesForTrackException, GetRecommendationsException {

        AudioFeatures track_features = TrackUtilities.getAudioFeaturesForTrack(spotify_api, uri);
        float tempo = track_features.getTempo();
        int target_duration_ms = track_features.getDurationMs();
        int offset = 1;
        int limit = 21;
        float margin_of_error = .1f; //

        while(true) {
            RecommendationArguments current_arguments = new RecommendationArguments(
                    spotify_api, limit, genres, seed_artists, seed_tracks,
                    tempo - offset, tempo + offset, tempo);

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
                String current_track_uri = current_track.getUri();

                // If the track we are considering is a duplicate continue to the next candidate
                if(track_map.get(current_track_uri) != null) continue;

                AudioFeatures current_features = getAudioFeaturesForTrack(spotify_api, current_track_uri);

                // Compare the durations of the closest track and the current, replacing the closest track with the
                // current if the current track is closer to the desired duration
                closest_track = getTrackWithClosestDuration(target_duration_ms, closest_track, current_track);
            }

            if(isGoodDuration(target_duration_ms, closest_track, margin_of_error)) return closest_track.getUri();

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
