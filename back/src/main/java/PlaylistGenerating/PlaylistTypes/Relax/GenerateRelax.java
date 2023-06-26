//package PlaylistGenerating.PlaylistTypes;
//
//import ExceptionClasses.BrowsingExceptions.GetRecommendationsException;
//import ExceptionClasses.PersonalizationExceptions.GetUsersTopArtistsRequestException;
//import ExceptionClasses.PersonalizationExceptions.GetUsersTopTracksRequestException;
//import ExceptionClasses.PlaylistExceptions.AddItemsToPlaylistException;
//import ExceptionClasses.PlaylistExceptions.CreatePlaylistException;
//import ExceptionClasses.ProfileExceptions.GetCurrentUsersProfileException;
//import PlaylistGenerating.HeartRateRanges.DesiredHeartRateRanges;
//import PlaylistGenerating.HeartRateRanges.TargetHeartRateRange;
//import se.michaelthelin.spotify.SpotifyApi;
//import se.michaelthelin.spotify.model_objects.specification.Playlist;
//
//import java.util.*;
//
//public class GenerateRelax extends GeneratePlaylist{
//    /**
//     * The user's entered resting heart rate
//     */
//    private int restHR;
//
//    /**
//     * An array of HRRange objects holding the descending intervals that start the playlist
//     */
//    private TargetHeartRateRange[] startTargetBPMs;
//    /**
//     * HR Range object containing the floor/target heart rate/tempo
//     */
//    private TargetHeartRateRange targetRangeBPM;
//
//
//    public GenerateRelax( int restHR, int lenPlaylist, String genre, SpotifyApi api)
//            throws GetRecommendationsException, AddItemsToPlaylistException, CreatePlaylistException,
//            GetCurrentUsersProfileException, GetUsersTopArtistsRequestException, GetUsersTopTracksRequestException {
//        super(lenPlaylist, genre, api);
//        if (restHR<75){
//            this.restHR= 75;
//        }else {
//            this.restHR = restHR;
//        }
//
//        this.targetRangeBPM = new TargetHeartRateRange(60,60,75);
//
//        // getting transitions for the descending intervals
//        this.startTargetBPMs = this.getTargetTransition();
//
//        // number of songs needed at the floor bpm 60-75
//
//        int numMaxIntervals = this.numSongs - startTargetBPMs.length ;
//
//        super.songsPerInterval.put(this.targetRangeBPM, numMaxIntervals);
//
////         gets Recommendations from spotify API then adds them to a newly created playlist in order
//        super.populateSongsToChoose();
//        this.constructPlaylist();
//    }
//    /**
//     * Helper method that creates the startTargetBPMs
//     * @return - the desired startTargetBPMs
//     */
//    private TargetHeartRateRange[] getTargetTransition() {
//
//        //floor/final heart rate
//        int toFinalHeartRate = this.restHR-60;
//        //average decrease in HR during meditation (high end) is 7 bpm/min
//        int numIntervals = (int) ((int) toFinalHeartRate/7);
//        //Make sure there is more than 0 intervals
//        if (numIntervals == 0 ){
//            numIntervals = 2;
//        }
//
//        // calculating how much each song would need to change by BPM
//        int bpmChange = toFinalHeartRate/numIntervals;
//        int offset = (bpmChange) / 2;
//
//        // getting the target BPMs for each interval in windup
//        TargetHeartRateRange[] targetStartIntervals = new TargetHeartRateRange[numIntervals];
//        for (int i = 0; i < numIntervals; i++){
//            int currTarget;
//
//                currTarget = this.restHR - (bpmChange * i);
//
//
//            // creating HeartRateRange object for each range and stores them using genPlaylist
//            TargetHeartRateRange currHR = DesiredHeartRateRanges.toHeartRateRange(currTarget, offset);
//            targetStartIntervals[i] = currHR;
//
//            // increment the numSongs for HR
//            super.addCountToInterval(currHR);
//
//        }
//        return targetStartIntervals;
//    }
//    /**
//     * This helper method uses super.songsToChoose (hashmap holding the recommendations) to add Start and Target songs
//     * to playlist using GeneratePlaylist's addToPlaylist method
//     * @throws CreatePlaylistException
//     * @throws AddItemsToPlaylistException
//     * @throws GetCurrentUsersProfileException
//     */
//    private void constructPlaylist() throws CreatePlaylistException, AddItemsToPlaylistException, GetCurrentUsersProfileException {
//        super.createPlaylist();
//
//        Map<TargetHeartRateRange, LinkedList<String>> songsCopy = this.getSongs();
//
//
//        //add to playlist
//        for (TargetHeartRateRange startBPM : this.startTargetBPMs){
//            super.addToPlaylist(startBPM, 1,songsCopy);
//        }
//
//        // shuffle all songs after getting to the floor 60 bpm
//        Collections.shuffle(this.songsToChoose.get(this.targetRangeBPM));
//
//        //add to playlist
//        super.addToPlaylist(this.targetRangeBPM, this.songsPerInterval.get(targetRangeBPM), songsCopy);
//
//    }
//    /**
//     * This helper method uses super.songsToChoose (hashmap holding the recommendations) to add Start and Target songs
//     * to playlist using GeneratePlaylist's addToPlaylist method
//
//     */
//    public Map<TargetHeartRateRange, LinkedList<String>> getSongs(){
//
//        Map<TargetHeartRateRange, LinkedList<String>> copy = new HashMap<>();
//
//        for (TargetHeartRateRange bpm : super.songsToChoose.keySet()){
//
//            LinkedList<String> lst = new LinkedList<>(super.songsToChoose.get(bpm));
//            copy.put(bpm, lst);
//        }
//        return copy;
//    }
//
//
//    @Override
//    public Playlist generatePlaylist() throws Exception {
//        return null;
//    }
//
//    @Override
//    protected int getTargetBPM() {
//        return 0;
//    }
//}
