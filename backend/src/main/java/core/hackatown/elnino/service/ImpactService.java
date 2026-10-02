package core.hackatown.elnino.service;

import core.hackatown.elnino.client.OpenMeteoClient;
import core.hackatown.elnino.config.BrazilGrid;
import core.hackatown.elnino.model.DailyWeather;
import core.hackatown.elnino.model.ImpactResponse;
import core.hackatown.elnino.model.Location;
import core.hackatown.elnino.model.Metric;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ImpactService {
    static final int BASELINE_YEARS = 3;
    static final int PERIOD_DAYS = 7;
    private static final ZoneId BRAZIL_TIME = ZoneId.of("America/Sao_Paulo");

    private final OpenMeteoClient client;
    private final ImpactCalculator calculator;
    private volatile CachedWeather cache;

    public ImpactService(OpenMeteoClient client, ImpactCalculator calculator) {
        this.client = client;
        this.calculator = calculator;
    }

    public ImpactResponse getImpacts(Metric metric) throws IOException, InterruptedException {
        LocalDate today = LocalDate.now(BRAZIL_TIME);
        LocalDate periodStart = today.minusDays(PERIOD_DAYS - 1L);
        CachedWeather weather = loadWeather(today);
        return new ImpactResponse(
                metric.name().toLowerCase(), metric.unit(), metric.description(),
                new ImpactResponse.PeriodInfo(
                        "últimos 7 dias (" + periodStart + " a " + today + ")",
                        "mesmas janelas em " + historicalYears(today) + " (média de 3 anos)"
                ),
                weather.loadedAt().toString(),
                calculator.calculate(metric, weather.current(), weather.baseline(), BASELINE_YEARS)
        );
    }

    private CachedWeather loadWeather(LocalDate today) throws IOException, InterruptedException {
        CachedWeather current = cache;
        if (current != null && current.date().equals(today) && !current.expired()) return current;

        synchronized (this) {
            current = cache;
            if (current != null && current.date().equals(today) && !current.expired()) return current;

            LocalDate periodStart = today.minusDays(PERIOD_DAYS - 1L);
            Map<Location, List<DailyWeather>> currentPeriod = client.fetchCurrentPeriod(
                    BrazilGrid.POINTS, periodStart, today);
            Map<Location, List<DailyWeather>> baseline = new LinkedHashMap<>();
            for (int yearsAgo = BASELINE_YEARS; yearsAgo >= 1; yearsAgo--) {
                LocalDate historicalEnd = today.minusYears(yearsAgo);
                LocalDate historicalStart = historicalEnd.minusDays(PERIOD_DAYS - 1L);
                merge(baseline, client.fetch(BrazilGrid.POINTS, historicalStart, historicalEnd));
            }
            cache = new CachedWeather(today, currentPeriod, baseline, Instant.now());
            return cache;
        }
    }

    private void merge(
            Map<Location, List<DailyWeather>> destination,
            Map<Location, List<DailyWeather>> source
    ) {
        source.forEach((location, values) -> destination
                .computeIfAbsent(location, ignored -> new ArrayList<>())
                .addAll(values));
    }

    private String historicalYears(LocalDate today) {
        return (today.getYear() - 3) + ", " + (today.getYear() - 2) + " e " + (today.getYear() - 1);
    }

    private record CachedWeather(
            LocalDate date,
            Map<Location, List<DailyWeather>> current,
            Map<Location, List<DailyWeather>> baseline,
            Instant loadedAt
    ) {
        boolean expired() {
            return loadedAt.plus(Duration.ofHours(6)).isBefore(Instant.now());
        }
    }
}
