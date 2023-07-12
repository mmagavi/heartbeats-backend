package PlaylistGenerating.PlaylistTypes.Relax;

import PlaylistGenerating.PlaylistTypes.Classic.GenerateClassic;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.Deque;

import static PlaylistGenerating.PlaylistTypes.Classic.GenerateClassic.*;
import static PlaylistGenerating.PlaylistTypes.Relax.GenerateRelax.*;

public class RelaxCheckingUtilities {

    /**
     * Checks if the track array provided is within the allowable duration range
     * SPECIFICALLY FOR WARMUP AND WIND-DOWN SEQUENCES ONLY
     *
     * @param tracks tracks to be checked for their duration
     * @return appropriately named enum (TOO_SHORT if too short, TOO_LONG if too long, and ACCEPTABLE if acceptable)
     */
    protected static DURATION_RESULT checkIntervalDuration(Deque<TrackSimplified> tracks) {
        int duration_ms = 0;

        for (TrackSimplified track : tracks) {
            duration_ms += track.getDurationMs();
        }

//        System.out.println("Duration: " + duration_ms);
//        System.out.println("Interval Duration: " + interval_len_ms);
//        System.out.println("Min Duration: " + min_interval_len_ms);
//        System.out.println("Max Duration: " + max_interval_len_ms);


        if (duration_ms < min_interval_len_ms) {
            return GenerateClassic.DURATION_RESULT.TOO_SHORT;
        } else if (duration_ms > max_interval_len_ms) {
            return GenerateClassic.DURATION_RESULT.TOO_LONG;
        } else {
            return GenerateClassic.DURATION_RESULT.ACCEPTABLE;
        }
    }

    /**
     * Checks if the track array provided is within the allowable duration range
     * SPECIFICALLY FOR TARGET SEQUENCE ONLY
     *
     * @param tracks tracks to be checked for their duration
     * @return appropriately named enum (TOO_SHORT if too short, TOO_LONG if too long, and ACCEPTABLE if acceptable)
     */
    protected static DURATION_RESULT checkTargetDuration(Deque<TrackSimplified> tracks) {
        int duration_ms = 0;

        for (TrackSimplified track : tracks) {
            duration_ms += track.getDurationMs();
        }

//        System.out.println("Duration: " + duration_ms);
//        System.out.println("Target Duration: " + target_len_ms);
//        System.out.println("Min Duration: " + min_target_len_ms);
//        System.out.println("Max Duration: " + max_target_len_ms);


        if (duration_ms < min_target_len_ms) {
            return GenerateClassic.DURATION_RESULT.TOO_SHORT;
        } else if (duration_ms > max_target_len_ms) {
            return GenerateClassic.DURATION_RESULT.TOO_LONG;
        } else {
            return GenerateClassic.DURATION_RESULT.ACCEPTABLE;
        }
    }
}
