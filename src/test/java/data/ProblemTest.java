package data;

import data.Problem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProblemTest {

    @Test
    public void testTimeConversion() {
        Problem.setUpProblem("basicTestData.json", true);
        double decimalHour = 12.3;
        double decimalDisc = 49.2;
        int discTimePoint = 112;
        int tippingTimePoint = 24 * 4;
        assertEquals(decimalDisc, Problem.hourToDiscDecimal(decimalHour), 0.0);
        assertEquals(49, Problem.hourToDiscTimePoint(decimalHour));
        assertEquals(decimalHour, Problem.discTimeToHour(decimalDisc), 0.0);
        assertEquals(16, Problem.discToDiscDayTime(discTimePoint));
        assertEquals(0, Problem.discToDiscDayTime(tippingTimePoint));

        Problem.setUpProblem("time/discretizationEight.json", true);

    }
}
