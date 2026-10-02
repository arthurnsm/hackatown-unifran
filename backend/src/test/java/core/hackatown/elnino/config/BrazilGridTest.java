package core.hackatown.elnino.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrazilGridTest {
    @Test
    void createsNationalGridWithExpectedDensity() {
        assertEquals(59, BrazilGrid.POINTS.size());
        assertTrue(BrazilGrid.POINTS.stream().allMatch(point ->
                point.latitude() >= -34 && point.latitude() <= 6
                        && point.longitude() >= -74 && point.longitude() <= -34));
    }
}
