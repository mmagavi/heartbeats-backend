package PlaylistGenerating.PlaylistTypes.Interval;

import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import static PlaylistGenerating.PlaylistTypes.Interval.GenerateIntervalOne.*;

public class IntervalCheckingUtilities {

    /**
     * Checks a combination of warm and cool songs to verify that they have a proper total
     * duration
     * @param warm_tracks - the warm tracks to check - aka songs for the high intensity part of the workout
     * @param cool_tracks - the 'cool' tracks to check - aka songs for the low intensity part of the workout
     * @return DURATION_RESULT enum value representing the result of the check
     */
    protected static GenerateIntervalOne.DURATION_RESULT checkPlaylistDuration(TrackSimplified[] cool_tracks, TrackSimplified[] warm_tracks) {
        int duration_ms = 0;

        for (TrackSimplified track : cool_tracks) {
            duration_ms += track.getDurationMs();
        }
        for (TrackSimplified track : warm_tracks) {
            duration_ms += track.getDurationMs();
        }

        System.out.println("Duration: " + duration_ms);
        System.out.println("Min Duration: " + min_target_length_ms);
        System.out.println("Max Duration: " + max_target_length_ms);

        int thirty_seconds_ms = 30_000;

        if (duration_ms < min_target_length_ms && duration_ms >= min_target_length_ms - thirty_seconds_ms) {
            return GenerateIntervalOne.DURATION_RESULT.WITHIN_THIRTY_SECONDS_SHORT;
        } else if (duration_ms > max_target_length_ms && duration_ms <= max_target_length_ms + thirty_seconds_ms) {
            return GenerateIntervalOne.DURATION_RESULT.WITHIN_THIRTY_SECONDS_LONG;
        } else if (duration_ms < min_target_length_ms) {
            return GenerateIntervalOne.DURATION_RESULT.TOO_SHORT;
        } else if (duration_ms > max_target_length_ms) {
            return GenerateIntervalOne.DURATION_RESULT.TOO_LONG;
        } else {
            return GenerateIntervalOne.DURATION_RESULT.ACCEPTABLE;
        }
    }
}
