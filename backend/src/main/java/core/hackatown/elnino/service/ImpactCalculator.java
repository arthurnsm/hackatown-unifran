package core.hackatown.elnino.service;

import core.hackatown.elnino.model.DailyWeather;
import core.hackatown.elnino.model.ImpactPoint;
import core.hackatown.elnino.model.Location;
import core.hackatown.elnino.model.Metric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class ImpactCalculator {
    private static final double MIN_RAIN_REFERENCE_MM = 5.0;

    public List<ImpactPoint> calculate(
            Metric metric,
            Map<Location, List<DailyWeather>> eventData,
            Map<Location, List<DailyWeather>> baselineData,
            int baselineYears
    ) {
        List<RawImpact> raw = new ArrayList<>();
        eventData.forEach((location, event) -> {
            List<DailyWeather> baseline = baselineData.getOrDefault(location, List.of());
            if (event.isEmpty() || baseline.isEmpty()) return;

            double eventValue;
            double historicalValue;
            if (metric == Metric.TEMPERATURE) {
                eventValue = event.stream().mapToDouble(DailyWeather::meanTemperature).average().orElseThrow();
                historicalValue = baseline.stream().mapToDouble(DailyWeather::meanTemperature).average().orElseThrow();
            } else {
                eventValue = event.stream().mapToDouble(DailyWeather::precipitation).sum();
                historicalValue = baseline.stream().mapToDouble(DailyWeather::precipitation).sum() / baselineYears;
            }

            double anomaly = metric == Metric.TEMPERATURE
                    ? eventValue - historicalValue
                    : percentageDifference(eventValue, historicalValue);
            double absoluteChange = eventValue - historicalValue;
            raw.add(new RawImpact(location, eventValue, historicalValue, anomaly, absoluteChange));
        });

        double maxMagnitude = raw.stream()
                .mapToDouble(item -> magnitude(metric, item))
                .max()
                .orElse(1.0);
        if (maxMagnitude == 0) maxMagnitude = 1.0;

        final double scale = maxMagnitude;
        return raw.stream()
                .map(item -> toPoint(metric, item, scale))
                .sorted(Comparator.comparingDouble(ImpactPoint::intensity).reversed())
                .toList();
    }

    private ImpactPoint toPoint(Metric metric, RawImpact item, double maxMagnitude) {
        Location location = item.location();
        return new ImpactPoint(
                location.id(), location.city(), location.state(),
                location.latitude(), location.longitude(),
                round(item.eventValue()), round(item.historicalValue()), round(item.anomaly()),
                round(item.absoluteChange()),
                metric != Metric.RAINFALL || item.historicalValue() >= MIN_RAIN_REFERENCE_MM,
                round(magnitude(metric, item) / maxMagnitude),
                direction(metric, item.anomaly())
        );
    }

    private double magnitude(Metric metric, RawImpact item) {
        return Math.abs(metric == Metric.RAINFALL ? item.absoluteChange() : item.anomaly());
    }

    private String direction(Metric metric, double anomaly) {
        if (metric == Metric.TEMPERATURE) return anomaly >= 0 ? "HOTTER" : "COOLER";
        return anomaly >= 0 ? "WETTER" : "DRIER";
    }

    private double percentageDifference(double value, double reference) {
        return reference == 0 ? 0 : ((value - reference) / reference) * 100.0;
    }

    private double round(double number) {
        return Math.round(number * 100.0) / 100.0;
    }

    private record RawImpact(
            Location location,
            double eventValue,
            double historicalValue,
            double anomaly,
            double absoluteChange
    ) {
    }
}
