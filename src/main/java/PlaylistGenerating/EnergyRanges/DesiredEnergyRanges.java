package PlaylistGenerating.EnergyRanges;

import java.util.Map;

public class DesiredEnergyRanges {

    private enum AGE_RANGE {
        TWENTIES_AND_UNDER, THIRTY_TO_THIRTY_FIVE, THIRTY_FIVE_TO_FORTY, FORTY_TO_FORTY_FIVE,
        FORTY_FIVE_TO_FIFTY, FIFTY_TO_FIFTY_FIVE, FIFTY_FIVE_TO_SIXTY, SIXTY_TO_SIXTY_FIVE, SIXTY_FIVE_TO_SEVENTY,
        SEVENTY_AND_UP
    }

    private final static EnergyRange twenties_and_under = new EnergyRange(.75f, .8f, .85f);
    private final static EnergyRange thirty_to_thirty_five = new EnergyRange(.78f, .83f, .88f);
    private final static EnergyRange thirty_five_to_forty = new EnergyRange(.76f, .81f, .86f);
    private final static EnergyRange forty_to_forty_five = new EnergyRange(.74f, .79f, .84f);
    private final static EnergyRange forty_five_to_fifty = new EnergyRange(.72f, .77f, .82f);
    private final static EnergyRange fifty_to_fifty_five = new EnergyRange(.7f, .75f, .80f);
    private final static EnergyRange fifty_five_to_sixty = new EnergyRange(.68f, .73f, .78f);
    private final static EnergyRange sixty_to_sixty_five = new EnergyRange(.66f, .71f, .76f);
    private final static EnergyRange sixty_five_to_seventy = new EnergyRange(.64f, .69f, .74f);
    private final static EnergyRange seventy_and_up = new EnergyRange(.62f, .67f, .72f);

    private static final Map<AGE_RANGE, EnergyRange> age_to_energy_map = Map.of
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
     * @return EnergyRange object for the provided age
     */
    public static EnergyRange getEnergyRange(int age) {

        if (age < 30) {
            return age_to_energy_map.get(AGE_RANGE.TWENTIES_AND_UNDER);
        }

        if (age > 70) {
            return age_to_energy_map.get(AGE_RANGE.SEVENTY_AND_UP);
        }

        // By subtracting 30 from the age of the user and dividing the result by 5 and finally adding 1 we
        // get the integer corresponding to the value of their age range in the AGE_RANGE enum

        int users_age_range_value = ((age - 30) / 5) + 1;

        return switch (users_age_range_value) {
            case 1 -> age_to_energy_map.get(AGE_RANGE.THIRTY_TO_THIRTY_FIVE);
            case 2 -> age_to_energy_map.get(AGE_RANGE.THIRTY_FIVE_TO_FORTY);
            case 3 -> age_to_energy_map.get(AGE_RANGE.FORTY_TO_FORTY_FIVE);
            case 4 -> age_to_energy_map.get(AGE_RANGE.FORTY_FIVE_TO_FIFTY);
            case 5 -> age_to_energy_map.get(AGE_RANGE.FIFTY_TO_FIFTY_FIVE);
            case 6 -> age_to_energy_map.get(AGE_RANGE.FIFTY_FIVE_TO_SIXTY);
            case 7 -> age_to_energy_map.get(AGE_RANGE.SIXTY_TO_SIXTY_FIVE);
            case 8 -> age_to_energy_map.get(AGE_RANGE.SIXTY_FIVE_TO_SEVENTY);
            default -> age_to_energy_map.get(AGE_RANGE.SEVENTY_AND_UP);
        };
    }
}
