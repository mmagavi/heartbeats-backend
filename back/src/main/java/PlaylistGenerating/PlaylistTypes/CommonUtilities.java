package PlaylistGenerating.PlaylistTypes;

import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

public class CommonUtilities {

    /**
     * Loops through all the given tracks and stores their IDs in a string array which is then returned
     *
     * @param tracks array of tracks to fetch the id from
     * @return String array of all the given track's ids, returns null if tracks is null
     */
    public static String[] getTrackIDs(TrackSimplified[] tracks) {

        if (tracks == null) return null;

        int size = tracks.length;

        String[] ids = new String[size];

        for (int index = 0; index < size; index++) {
            ids[index] = tracks[index].getUri();
            //ids[index] = tracks[index].getId();
        }

        return ids;
    }
}
