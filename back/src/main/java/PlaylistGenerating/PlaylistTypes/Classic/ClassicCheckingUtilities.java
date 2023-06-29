package PlaylistGenerating.PlaylistTypes.Classic;

import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.Deque;
import java.util.HashMap;

import static PlaylistGenerating.PlaylistTypes.Classic.ClassicTrackUtilities.getTracksInColumn;
import static PlaylistGenerating.PlaylistTypes.Classic.GenerateClassic.*;

public class ClassicCheckingUtilities {

    /**
     * Checks if the track array provided is within the allowable duration range
     * SPECIFICALLY FOR WARMUP AND WIND-DOWN SEQUENCES ONLY
     *
     * @param tracks tracks to be checked for their duration
     * @return appropriately named enum (TOO_SHORT if too short, TOO_LONG if too long, and ACCEPTABLE if acceptable)
     */
    protected static GenerateClassic.DURATION_RESULT checkTransitionDuration(TrackSimplified[] tracks) {
        int duration_ms = 0;

        for (TrackSimplified track : tracks) {
            duration_ms += track.getDurationMs();
        }

        System.out.println("Duration: " + duration_ms);
        System.out.println("Transition Duration: " + transition_length_ms);
        System.out.println("Min Duration: " + min_transition_length_ms);
        System.out.println("Max Duration: " + max_transition_length_ms);

        int thirty_seconds_ms = 30_000;

        if (duration_ms < min_transition_length_ms && duration_ms >= min_transition_length_ms - thirty_seconds_ms) {
            return GenerateClassic.DURATION_RESULT.WITHIN_THIRTY_SECONDS_SHORT;
        } else if (duration_ms > max_transition_length_ms && duration_ms <= max_transition_length_ms + thirty_seconds_ms) {
            return GenerateClassic.DURATION_RESULT.WITHIN_THIRTY_SECONDS_LONG;
        } else if (duration_ms < min_transition_length_ms) {
            return GenerateClassic.DURATION_RESULT.TOO_SHORT;
        } else if (duration_ms > max_transition_length_ms) {
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
    protected static GenerateClassic.DURATION_RESULT checkTargetDuration(Deque<TrackSimplified> tracks) {
        int duration_ms = 0;

        for (TrackSimplified track : tracks) {
            duration_ms += track.getDurationMs();
        }

        System.out.println("Duration: " + duration_ms);
        System.out.println("Target Duration: " + target_length_ms);
        System.out.println("Min Duration: " + min_target_length_ms);
        System.out.println("Max Duration: " + max_target_length_ms);

        if (duration_ms < min_target_length_ms) {
            return GenerateClassic.DURATION_RESULT.TOO_SHORT;
        } else if (duration_ms > max_target_length_ms) {
            return GenerateClassic.DURATION_RESULT.TOO_LONG;
        } else {
            return GenerateClassic.DURATION_RESULT.ACCEPTABLE;
        }
    }

    /**
     * Checks columns to the right of the given column in the TrackSimplified arrays in the intervals map for better
     * fitting columns
     *
     * @param column column to start from
     * @return best fitting column
     */
    protected static int checkForLongerColumn(HashMap<Integer, TrackSimplified[]> intervals, int column) {

        TrackSimplified[] next_column_tracks;
        DURATION_RESULT result;

        do {

            // if the next column is out of bounds return the current column
            if (column + 1 >= limit) return column;

            next_column_tracks = getTracksInColumn(intervals, column + 1);

            result = checkTransitionDuration(next_column_tracks);

            // if the next column is a good fit, return it
            if (result == DURATION_RESULT.ACCEPTABLE) return column + 1;

            // if the result of the next column is now too long the current column is the most ideal
            if (result == DURATION_RESULT.TOO_LONG || result == DURATION_RESULT.WITHIN_THIRTY_SECONDS_LONG) {
                return column;
            } else {
                column++; // if the next column is not too long keep searching
            }

        } while (true);
    }

    /**
     * Checks columns to the left of the given column in the TrackSimplified arrays in the intervals map for better
     * fitting columns
     *
     * @param column column to start from
     * @return best fitting column
     */
    protected static int checkForShorterColumn(HashMap<Integer, TrackSimplified[]> intervals, int column) {

        TrackSimplified[] next_column_tracks;
        DURATION_RESULT result;

        do {

            // if the next column is out of bounds return the current column
            if (column - 1 < 0) return column;

            next_column_tracks = getTracksInColumn(intervals,column - 1);

            result = checkTransitionDuration(next_column_tracks);

            // if the next column is a good fit, return it
            if (result == DURATION_RESULT.ACCEPTABLE) return column - 1;

            // if the result of the next column is now too short the current column is the most ideal
            if (result == DURATION_RESULT.TOO_SHORT || result == DURATION_RESULT.WITHIN_THIRTY_SECONDS_SHORT) {
                return column;
            } else {
                column--; // if the next column is not too long keep searching
            }

        } while (true);
    }
}
