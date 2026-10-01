package core.hackatown.elnino.service;

import core.hackatown.elnino.client.OpenMeteoClient;
import core.hackatown.elnino.config.BrazilLocations;
import core.hackatown.elnino.model.DailyWeather;
import core.hackatown.elnino.model.ImpactResponse;
import core.hackatown.elnino.model.Location;
import core.hackatown.elnino.model.Metric;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class ImpactService {
    static final LocalDate EVENT_START = LocalDate.of(2023, 6, 1);
    static final LocalDate EVENT_END = LocalDate.of(2024, 5, 31);
    static final LocalDate BASELINE_START = LocalDate.of(2013, 6, 1);
    static final LocalDate BASELINE_END = LocalDate.of(2023, 5, 31);
    static final int BASELINE_YEARS = 10;

    private final OpenMeteoClient client;
    private final ImpactCalculator calculator;
    private volatile CachedWeather cache;

    public ImpactService(OpenMeteoClient client, ImpactCalculator calculator) {
        this.client = client;
        this.calculator = calculator;
    }

    public ImpactResponse getImpacts(Metric metric) throws IOException, InterruptedException {
        CachedWeather weather = loadWeather();
        return new ImpactResponse(
                metric.name().toLowerCase(), metric.unit(), metric.description(),
                new ImpactResponse.PeriodInfo(
                        EVENT_START + " a " + EVENT_END,
                        BASELINE_START + " a " + BASELINE_END + " (média de 10 períodos anuais)"
                ),
                weather.loadedAt().toString(),
                calculator.calculate(metric, weather.event(), weather.baseline(), BASELINE_YEARS)
        );
    }

    private CachedWeather loadWeather() throws IOException, InterruptedException {
        CachedWeather current = cache;
        if (current != null && !current.expired()) return current;

        synchronized (this) {
            current = cache;
            if (current != null && !current.expired()) return current;

            Map<Location, List<DailyWeather>> event = client.fetch(
                    BrazilLocations.CAPITALS, EVENT_START, EVENT_END);
            Map<Location, List<DailyWeather>> baseline = client.fetch(
                    BrazilLocations.CAPITALS, BASELINE_START, BASELINE_END);
            cache = new CachedWeather(event, baseline, Instant.now());
            return cache;
        }
    }

    private record CachedWeather(
            Map<Location, List<DailyWeather>> event,
            Map<Location, List<DailyWeather>> baseline,
            Instant loadedAt
    ) {
        boolean expired() {
            return loadedAt.plus(Duration.ofHours(6)).isBefore(Instant.now());
        }
    }
}
