package PlaylistGenerating.PlaylistTypes.Relax;

import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
import ExceptionClasses.PlaylistExceptions.AddItemsToPlaylistException;
import ExceptionClasses.PlaylistExceptions.CreatePlaylistException;
import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
import PlaylistGenerating.HeartRateRanges.DesiredHeartRateRanges;
import PlaylistGenerating.HeartRateRanges.TargetHeartRateRange;
import PlaylistGenerating.PlaylistTypes.GeneratePlaylist;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.*;

public class GenerateRelax extends GeneratePlaylist {

    private final int num_intervals;
    private int workout_len_ms;
    private int min_workout_len_ms;
    private int max_workout_len_ms;
    private final int limit = 11;

    public GenerateRelax(SpotifyApi spotify_api, String genres, int age, int workout_length, String intensity)
    throws GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException, GetCurrentUsersProfileException {
        super(spotify_api, genres, age, workout_length, intensity);

        num_intervals = Math.round(workout_length * .1f); // Each interval should be about 10 minutes
        workout_len_ms = workout_length * 60_000;
        setWorkoutLengths(margin_of_error);

    }

    @Override
    public String generatePlaylist() throws Exception {
        return null;
    }

    private TrackSimplified[] findSongs(){

        // difference between target bpm for each interval
        int bpm_difference = ((target_bpm - resting_bpm) / num_intervals);

        for(int interval = 0; interval < num_intervals; interval++){

        }

        return null;
    }

    /**
     * Sets the min and max transition lengths based on the provided margin of error
     *
     * @param margin_of_error margin of error used in setting the min and max transition lengths
     */
    private void setWorkoutLengths(float margin_of_error) {

        // MilliSeconds of length that is acceptable for the warmup / wind-down sequence based on our MOE
        min_workout_len_ms = workout_len_ms - (int) (workout_len_ms * margin_of_error);
        max_workout_len_ms = workout_len_ms + (int) (workout_len_ms * margin_of_error);
    }
}
