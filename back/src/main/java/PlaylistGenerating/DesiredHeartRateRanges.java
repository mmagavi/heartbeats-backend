package PlaylistGenerating;

import java.util.Map;

public class DesiredHeartRateRanges {

    private enum AGE_RANGE {
        TWENTIES_AND_UNDER, THIRTY_TO_THIRTY_FIVE, THIRTY_FIVE_TO_FORTY, FORTY_TO_FORTY_FIVE,
        FORTY_FIVE_TO_FIFTY, FIFTY_TO_FIFTY_FIVE, FIFTY_FIVE_TO_SIXTY, SIXTY_TO_SIXTY_FIVE, SIXTY_FIVE_TO_SEVENTY,
        SEVENTY_AND_UP

    }

    private final static HeartRateRange twenty_and_under = new HeartRateRange(125, 135, 145);
    private final static HeartRateRange thirty_to_thirty_five = new HeartRateRange(118, 128, 138);
    private final static HeartRateRange thirty_five_to_forty = new HeartRateRange(115, 125, 135);
    private final static HeartRateRange forty_to_forty_five = new HeartRateRange(111, 121, 131);
    private final static HeartRateRange forty_five_to_fifty = new HeartRateRange(108, 118, 128);
    private final static HeartRateRange fifty_to_fifty_five = new HeartRateRange(105, 115, 125);
    private final static HeartRateRange fifty_five_to_sixty = new HeartRateRange(101, 111, 121);
    private final static HeartRateRange sixty_to_sixty_five = new HeartRateRange(98, 108, 118);
    private final static HeartRateRange sixty_five_to_seventy = new HeartRateRange(95, 105, 115);
    private final static HeartRateRange seventy_and_up = new HeartRateRange(95, 105, 115);

    private static final Map<AGE_RANGE, HeartRateRange> age_to_heart_rate_map = Map.of
            (AGE_RANGE.TWENTIES_AND_UNDER, twenty_and_under,
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
     * @return HeartRateRange object for the provided age
     */
    public static HeartRateRange getHeartRateRange(int age) {

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

    public static HeartRateRange toHeartRateRange(int target, int offset) {
        return new HeartRateRange(target - offset, target, target + offset);
    }
}
