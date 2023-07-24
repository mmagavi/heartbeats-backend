package PlaylistGenerating.PlaylistTypes.Interval;

import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.Deque;

import static PlaylistGenerating.PlaylistTypes.Interval.GenerateIntervalOne.*;

public class IntervalCheckingUtilities {

    /**
     * Checks a combination of tracks to verify that they have a proper total
     * duration
     * @param tracks tracks to be checked for their duration
     * @return DURATION_RESULT enum value representing the result of the check
     */
    protected static DURATION_RESULT checkTotalDuration(TrackSimplified[] tracks) {
        int duration_ms = 0;

        for (TrackSimplified track : tracks) {
            duration_ms += track.getDurationMs();
        }

        System.out.println("Duration: " + duration_ms);
        System.out.println("Min Duration: " + min_target_len_ms);
        System.out.println("Max Duration: " + max_target_len_ms);

        int thirty_seconds_ms = 30_000;

        if (duration_ms < min_target_len_ms && duration_ms >= min_target_len_ms - thirty_seconds_ms) {
            return DURATION_RESULT.WITHIN_THIRTY_SECONDS_SHORT;
        } else if (duration_ms > max_target_len_ms && duration_ms <= max_target_len_ms + thirty_seconds_ms) {
            return DURATION_RESULT.WITHIN_THIRTY_SECONDS_LONG;
        } else if (duration_ms < min_target_len_ms) {
            return DURATION_RESULT.TOO_SHORT;
        } else if (duration_ms > max_target_len_ms) {
            return DURATION_RESULT.TOO_LONG;
        } else {
            return DURATION_RESULT.ACCEPTABLE;
        }
    }

    /**
     * Checks if the track array provided is within the allowable duration range
     * SPECIFICALLY FOR INTERVALS
     *
     * @param tracks tracks to be checked for their duration
     * @return appropriately named enum (TOO_SHORT if too short, TOO_LONG if too long, and ACCEPTABLE if acceptable)
     */
    protected static DURATION_RESULT checkIntervalDuration(Deque<TrackSimplified> tracks) {
        int duration_ms = 0;

        for (TrackSimplified track : tracks) {
            duration_ms += track.getDurationMs();
        }

        System.out.println("Duration: " + duration_ms);
        System.out.println("Target Duration: " + interval_len_ms);
        System.out.println("Min Duration: " + min_interval_len_ms);
        System.out.println("Max Duration: " + max_interval_len_ms);

        if (duration_ms < min_interval_len_ms) {
            return DURATION_RESULT.TOO_SHORT;
        } else if (duration_ms > max_interval_len_ms) {
            return DURATION_RESULT.TOO_LONG;
        } else {
            return DURATION_RESULT.ACCEPTABLE;
        }
    }
}
