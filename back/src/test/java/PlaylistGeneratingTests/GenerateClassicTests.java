package PlaylistGeneratingTests;

import PlaylistGenerating.PlaylistTypes.GenerateClassic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenerateClassicTests {

    @Test
    public void testFindBpmDifference() throws Exception {

        Method method = GenerateClassic.class.getDeclaredMethod("findBpmDifference");
        method.setAccessible(true);

        int target_bpm = 105;

        // Test special case of less than 45-minute workouts
        GenerateClassic generateClassic =
                new GenerateClassic(null, "hip-hop", 33, 44, "low");


        assertEquals(18f, method.invoke(generateClassic));

        // All other durations above 45 minutes treated slightly differently
        generateClassic = new GenerateClassic(null, "hip-hop", 15, 177, "high");

        assertEquals(16f, method.invoke(generateClassic));
    }
}
