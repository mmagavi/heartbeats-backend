//package PlaylistGenerating.PlaylistTypes;
//
//import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
//import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
//import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
//import ExceptionClasses.PlaylistExceptions.AddItemsToPlaylistException;
//import ExceptionClasses.PlaylistExceptions.CreatePlaylistException;
//import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
//import PlaylistGenerating.DesiredHeartRateRanges;
//import PlaylistGenerating.TargetHeartRateRange;
//import se.michaelthelin.spotify.SpotifyApi;
//import java.util.*;
//
//public class GenerateExercise extends GeneratePlaylist {
//    /**
//     * The resting BPM that the user enters.
//     */
//    private final int restBPM;
//
//    /**
//     * The peak BPM for exercising we calculate give the user's age.
//     */
//    private final TargetHeartRateRange targetRangeBPM;
//
//    /**
//     * Contains an array of the HeartRateRanges going from the resting BPM up to the peak BPM
//     * i.e. going from 60 -> 100 & !quickStart: [60, 70, 80, 90]
//     */
//    private final TargetHeartRateRange[] startTargetBPMs;
//    /**
//     * Contains an array of the HeartRateRanges going from the peak BPM down to the resting BPM
//     * i.e. going from 100 -> 60 & quickEnd: [80, 60]
//     */
//    private final TargetHeartRateRange[] endTargetBPMs;
//
//    public GenerateExercise(int age, int restBPM, boolean isQuickStart, boolean isQuickEnd, int lenPlaylist,
//                            String genre, SpotifyApi api) throws GetRecommendationsException, GetCurrentUsersProfileException, CreatePlaylistException, AddItemsToPlaylistException, GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException {
//
//        super(lenPlaylist, genre, api);
//
//        this.restBPM = restBPM;
//        this.targetRangeBPM = DesiredHeartRateRanges.getTargetHeartRateRange(Math.abs(age));
//
//        // songs needed for wind up and wind down.
//        this.startTargetBPMs = this.getTargetTransitions(true, isQuickStart);
//        this.endTargetBPMs = this.getTargetTransitions(false, isQuickEnd);
//
//        // number of songs needed at the max bpm
//        // change if length of playlist is also adjustable to < 30 mins
//        int numMaxIntervals = this.numSongs - startTargetBPMs.length - endTargetBPMs.length;
//        super.songsPerInterval.put(this.targetRangeBPM, numMaxIntervals);
//
//        // create the playlist so that other classes can call the results with (.getPlaylist())
//        //      > note: that is in the parent class GeneratePlaylist
//        super.populateSongsToChoose();
//        this.constructPlaylist();
//
//    }
//
//    /**
//     * Helper method that creates the startTargetBPMs and endTargetBPMs
//     * @param isStart - true if creating startTargetBPMs, false for endTargetBPMs
//     * @param isQuick - true if is quickStart/quickEnd, false otherwise
//     * @return - the desired startTargetBPMs or endTargetBPMs
//     */
//    private TargetHeartRateRange[] getTargetTransitions(boolean isStart, boolean isQuick) {
//        int numIntervals;
//
//        if (isQuick) {
//            numIntervals = 2; // 2 songs if quick
//        } else {
//            numIntervals = 4; // 4 songs if long
//        }
//
//        // calculating how much each song would need to change by BPM
//        int bpmChange = (this.targetRangeBPM.target_heart_rate() - this.restBPM)/numIntervals;
//        int offset = (bpmChange - 1) / 2;
//
//        // getting the target BPMs for each interval in windup
//        TargetHeartRateRange[] targetStartIntervals = new TargetHeartRateRange[numIntervals];
//
//        for (int i = 0; i < numIntervals; i++){
//            int currTarget;
//            if (isStart) {
//                // [restBPM, restBPM + c, restBPM + 2c, ..., targetRangeBPM - c]
//                currTarget = this.restBPM + (bpmChange * i);
//            } else {
//                // [targetRangeBPM - c, targetRangeBPM - 2c, ..., restBPM]
//                currTarget = this.targetRangeBPM.target_heart_rate() - (bpmChange * (i+1));
//            }
//
//            // converting just the bpm from an Integer to a HeartRateRange obj
//            TargetHeartRateRange currBPM = DesiredHeartRateRanges.toHeartRateRange(currTarget, offset);
//            targetStartIntervals[i] = currBPM;
//
//            // increment the hashmap
//            super.addCountToInterval(currBPM);
//
//        }
//        return targetStartIntervals;
//    }
//    public Map<TargetHeartRateRange, LinkedList<String>> getSongs(){
//        Map<TargetHeartRateRange, LinkedList<String>> copy = new HashMap<>();
//        for (TargetHeartRateRange bpm : super.songsToChoose.keySet()){
//            LinkedList<String> lst = new LinkedList<>(super.songsToChoose.get(bpm));
//            copy.put(bpm, lst);
//        }
//        return copy;
//    }
//
//
//    /**
//     * Using super.songsToChoose (hashmap holding the recommenedations) to actually pick and place into a playlist
//     * @throws CreatePlaylistException
//     * @throws AddItemsToPlaylistException
//     * @throws GetCurrentUsersProfileException
//     */
//    private void constructPlaylist() throws CreatePlaylistException, AddItemsToPlaylistException, GetCurrentUsersProfileException {
//        super.createPlaylist();
//
//        Map<TargetHeartRateRange, LinkedList<String>> songsCopy = this.getSongs();
//        // adding startBPMs songs to playlist
//        for (TargetHeartRateRange startBPM : this.startTargetBPMs){
//            // System.out.println(startBPM);
//            super.addToPlaylist(startBPM, 1, songsCopy);
//        }
//
//        // shuffle the targetRangeBPM songs
//        Collections.shuffle(songsCopy.get(this.targetRangeBPM));
//
//        // adding targetRangeBPM songs to playlist
//        super.addToPlaylist(this.targetRangeBPM, this.songsPerInterval.get(targetRangeBPM), songsCopy);
//
//        // adding endBPMs songs to playlist
//        for (TargetHeartRateRange endBPM: this.endTargetBPMs) {
//            super.addToPlaylist(endBPM, 1, songsCopy);
//        }
//    }
//}
