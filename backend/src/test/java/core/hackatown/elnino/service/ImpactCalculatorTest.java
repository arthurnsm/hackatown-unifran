package core.hackatown.elnino.service;

import core.hackatown.elnino.model.DailyWeather;
import core.hackatown.elnino.model.ImpactPoint;
import core.hackatown.elnino.model.Location;
import core.hackatown.elnino.model.Metric;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImpactCalculatorTest {
    private final Location city = new Location("XX", "Cidade", "Estado", -10, -50);
    private final ImpactCalculator calculator = new ImpactCalculator();

    @Test
    void calculatesTemperatureAnomaly() {
        ImpactPoint point = calculator.calculate(
                Metric.TEMPERATURE,
                Map.of(city, List.of(day(24, 10), day(26, 20))),
                Map.of(city, List.of(day(20, 10), day(22, 20))),
                1
        ).get(0);

        assertEquals(25, point.currentValue());
        assertEquals(21, point.historicalValue());
        assertEquals(4, point.anomaly());
        assertEquals(1, point.intensity());
        assertEquals("HOTTER", point.direction());
    }

    @Test
    void calculatesRainfallPercentageAgainstAnnualAverage() {
        ImpactPoint point = calculator.calculate(
                Metric.RAINFALL,
                Map.of(city, List.of(day(20, 150))),
                Map.of(city, List.of(day(20, 100), day(20, 100))),
                2
        ).get(0);

        assertEquals(150, point.currentValue());
        assertEquals(100, point.historicalValue());
        assertEquals(50, point.anomaly());
        assertEquals("WETTER", point.direction());
    }

    private DailyWeather day(double temperature, double rain) {
        return new DailyWeather(LocalDate.of(2024, 1, 1), temperature, rain);
    }
}
