package PlaylistGenerating.HeartRateRanges;

import java.util.Map;

// These numbers for optimal heart rate ranges are pulled from the Mayo Clinic at the link below
// https://www.mayoclinic.org/healthy-lifestyle/fitness/in-depth/exercise-intensity/art-20046887

public class DesiredHeartRateRanges {

    private enum AGE_RANGE {
        TWENTIES_AND_UNDER, THIRTY_TO_THIRTY_FIVE, THIRTY_FIVE_TO_FORTY, FORTY_TO_FORTY_FIVE,
        FORTY_FIVE_TO_FIFTY, FIFTY_TO_FIFTY_FIVE, FIFTY_FIVE_TO_SIXTY, SIXTY_TO_SIXTY_FIVE, SIXTY_FIVE_TO_SEVENTY,
        SEVENTY_AND_UP
    }

    private final static TargetHeartRateRange twenties_and_under = new TargetHeartRateRange(110, 125, 140);
    private final static TargetHeartRateRange thirty_to_thirty_five = new TargetHeartRateRange(105, 133, 162);
    private final static TargetHeartRateRange thirty_five_to_forty = new TargetHeartRateRange(103, 130, 157);
    private final static TargetHeartRateRange forty_to_forty_five = new TargetHeartRateRange(100, 126, 153);
    private final static TargetHeartRateRange forty_five_to_fifty = new TargetHeartRateRange(98, 124, 149);
    private final static TargetHeartRateRange fifty_to_fifty_five = new TargetHeartRateRange(95, 120, 145);
    private final static TargetHeartRateRange fifty_five_to_sixty = new TargetHeartRateRange(93, 117, 140);
    private final static TargetHeartRateRange sixty_to_sixty_five = new TargetHeartRateRange(90, 113, 136);
    private final static TargetHeartRateRange sixty_five_to_seventy = new TargetHeartRateRange(88, 110, 132);
    private final static TargetHeartRateRange seventy_and_up = new TargetHeartRateRange(85, 107, 128);

    private static final Map<AGE_RANGE, TargetHeartRateRange> age_to_heart_rate_map = Map.of
            (AGE_RANGE.TWENTIES_AND_UNDER, twenties_and_under,
                    AGE_RANGE.THIRTY_TO_THIRTY_FIVE, thirty_to_thirty_five,
                    AGE_RANGE.THIRTY_FIVE_TO_FORTY, thirty_five_to_forty,
                    AGE_RANGE.FORTY_TO_FORTY_FIVE, forty_to_forty_five,
                    AGE_RANGE.FORTY_FIVE_TO_FIFTY, forty_five_to_fifty,
                    AGE_RANGE.FIFTY_TO_FIFTY_FIVE, fifty_to_fifty_five,
                    AGE_RANGE.FIFTY_FIVE_TO_SIXTY, fifty_five_to_sixty,
                    AGE_RANGE.SIXTY_TO_SIXTY_FIVE, sixty_to_sixty_five,
                    AGE_RANGE.SIXTY_FIVE_TO_SEVENTY, sixty_five_to_seventy,
                    AGE_RANGE.SEVENTY_AND_UP, seventy_and_up);

    /**
     * Takes in an int age and returns a corresponding HeartRateRange object
     *
     * @param age provided age
     * @return TargetHeartRateRange object for the provided age
     */
    public static TargetHeartRateRange getTargetHeartRateRange(int age) {

        if (age < 30) {
            return age_to_heart_rate_map.get(AGE_RANGE.TWENTIES_AND_UNDER);
        }

        if (age > 70) {
            return age_to_heart_rate_map.get(AGE_RANGE.SEVENTY_AND_UP);
        }

        // By subtracting 30 from the age of the user and dividing the result by 5 and finally adding 1 we
        // get the integer corresponding to the value of their age range in the AGE_RANGE enum

        int users_age_range_value = ((age - 30) / 5) + 1;

        return switch (users_age_range_value) {
            case 1 -> age_to_heart_rate_map.get(AGE_RANGE.THIRTY_TO_THIRTY_FIVE);
            case 2 -> age_to_heart_rate_map.get(AGE_RANGE.THIRTY_FIVE_TO_FORTY);
            case 3 -> age_to_heart_rate_map.get(AGE_RANGE.FORTY_TO_FORTY_FIVE);
            case 4 -> age_to_heart_rate_map.get(AGE_RANGE.FORTY_FIVE_TO_FIFTY);
            case 5 -> age_to_heart_rate_map.get(AGE_RANGE.FIFTY_TO_FIFTY_FIVE);
            case 6 -> age_to_heart_rate_map.get(AGE_RANGE.FIFTY_FIVE_TO_SIXTY);
            case 7 -> age_to_heart_rate_map.get(AGE_RANGE.SIXTY_TO_SIXTY_FIVE);
            case 8 -> age_to_heart_rate_map.get(AGE_RANGE.SIXTY_FIVE_TO_SEVENTY);
            default -> age_to_heart_rate_map.get(AGE_RANGE.SEVENTY_AND_UP);
        };
    }
}
