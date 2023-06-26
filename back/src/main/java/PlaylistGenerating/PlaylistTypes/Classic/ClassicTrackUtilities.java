package PlaylistGenerating.PlaylistTypes.Classic;

import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import static PlaylistGenerating.PlaylistTypes.Classic.GenerateClassic.num_intervals;

public class ClassicTrackUtilities {

    /**
     * Gets each track in the specified column from each interval in the provided intervals argument
     *
     * @param column desired column to fetch a track from each interval
     * @return TrackSimplified array of the songs from the requested column in each interval
     */
    protected static TrackSimplified[] getTracksInColumn(HashMap<Integer, TrackSimplified[]> intervals, int column) {

        TrackSimplified[] return_tracks = new TrackSimplified[num_intervals];

        TrackSimplified[] current_interval_tracks;

        for (int interval = 0; interval < num_intervals; interval++) {
            current_interval_tracks = intervals.get(interval);

            return_tracks[interval] = current_interval_tracks[column];
        }

        return return_tracks;
    }

    /**
     * @param arrays array arguments to concat
     * @return String array of the concatenated arguments
     */
    protected static String[] concatTracks(String[]... arrays) {

        Stream<String> stream = Stream.of();

        for (String[] array : arrays) {
            stream = Stream.concat(stream, Arrays.stream(array));
        }

        return stream.toArray(String[]::new);

    }
}
