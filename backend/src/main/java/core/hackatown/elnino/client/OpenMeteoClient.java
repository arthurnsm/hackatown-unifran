package core.hackatown.elnino.client;

import core.hackatown.elnino.model.DailyWeather;
import core.hackatown.elnino.model.ForecastDay;
import core.hackatown.elnino.model.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class OpenMeteoClient {
    private static final String ARCHIVE_URL = "https://archive-api.open-meteo.com/v1/archive";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    private static final int BATCH_SIZE = 25;

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public OpenMeteoClient(ObjectMapper mapper) {
        this.mapper = mapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public Map<Location, List<DailyWeather>> fetch(
            List<Location> locations, LocalDate start, LocalDate end
    ) throws IOException, InterruptedException {
        return fetch(locations, start, end, ARCHIVE_URL, true);
    }

    public Map<Location, List<DailyWeather>> fetchCurrentPeriod(
            List<Location> locations, LocalDate start, LocalDate end
    ) throws IOException, InterruptedException {
        return fetch(locations, start, end, FORECAST_URL, false);
    }

    private Map<Location, List<DailyWeather>> fetch(
            List<Location> locations, LocalDate start, LocalDate end,
            String baseUrl, boolean useEra5
    ) throws IOException, InterruptedException {
        Map<Location, List<DailyWeather>> result = new LinkedHashMap<>();
        for (int offset = 0; offset < locations.size(); offset += BATCH_SIZE) {
            int endIndex = Math.min(offset + BATCH_SIZE, locations.size());
            result.putAll(fetchBatch(
                    locations.subList(offset, endIndex), start, end, baseUrl, useEra5));
        }
        return result;
    }

    private Map<Location, List<DailyWeather>> fetchBatch(
            List<Location> locations, LocalDate start, LocalDate end,
            String baseUrl, boolean useEra5
    ) throws IOException, InterruptedException {
        String latitudes = locations.stream()
                .map(location -> format(location.latitude()))
                .collect(Collectors.joining(","));
        String longitudes = locations.stream()
                .map(location -> format(location.longitude()))
                .collect(Collectors.joining(","));

        String url = baseUrl
                + "?latitude=" + latitudes
                + "&longitude=" + longitudes
                + "&start_date=" + start
                + "&end_date=" + end
                + "&daily=temperature_2m_mean,precipitation_sum"
                + (useEra5 ? "&models=era5" : "")
                + "&timezone=GMT";

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(120))
                .header("Accept", "application/json")
                .header("User-Agent", "Hackatown-El-Nino/1.0")
                .GET()
                .build();

        HttpResponse<String> response = sendWithRateLimitRetry(request);
        if (response.statusCode() != 200) {
            throw new IOException("Open-Meteo respondeu HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        List<JsonNode> responses = new ArrayList<>();
        if (root.isArray()) root.forEach(responses::add);
        else responses.add(root);

        if (responses.size() != locations.size()) {
            throw new IOException("A Open-Meteo retornou uma quantidade inesperada de locais");
        }

        Map<Location, List<DailyWeather>> result = new LinkedHashMap<>();
        for (int i = 0; i < locations.size(); i++) {
            result.put(locations.get(i), parseDaily(responses.get(i)));
        }
        return result;
    }

    private HttpResponse<String> sendWithRateLimitRetry(HttpRequest request)
            throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 2; attempt++) {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 429 || attempt == 1) return response;
            Thread.sleep(65_000L);
        }
        throw new IllegalStateException("Fluxo de repetição inválido");
    }

    public ForecastDay fetchTomorrowForecast(Location location) throws IOException, InterruptedException {
        String url = FORECAST_URL + "?latitude=" + format(location.latitude())
                + "&longitude=" + format(location.longitude())
                + "&daily=temperature_2m_mean,precipitation_sum&forecast_days=2&timezone=America%2FSao_Paulo";
        HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20)).header("Accept", "application/json").GET().build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("Open-Meteo forecast respondeu HTTP " + response.statusCode());
        JsonNode daily = mapper.readTree(response.body()).path("daily");
        if (daily.path("time").size() < 2 || daily.path("temperature_2m_mean").size() < 2 || daily.path("precipitation_sum").size() < 2) {
            throw new IOException("Open-Meteo não retornou a previsão de amanhã");
        }
        return new ForecastDay(LocalDate.parse(daily.path("time").get(1).asText()),
                daily.path("temperature_2m_mean").get(1).asDouble(), daily.path("precipitation_sum").get(1).asDouble());
    }

    private List<DailyWeather> parseDaily(JsonNode response) throws IOException {
        JsonNode daily = response.path("daily");
        JsonNode dates = daily.path("time");
        JsonNode temperatures = daily.path("temperature_2m_mean");
        JsonNode precipitation = daily.path("precipitation_sum");
        if (!dates.isArray() || !temperatures.isArray() || !precipitation.isArray()) {
            throw new IOException("Resposta da Open-Meteo sem as séries diárias esperadas");
        }

        List<DailyWeather> values = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            JsonNode temperature = temperatures.get(i);
            JsonNode rain = precipitation.get(i);
            if (temperature != null && rain != null && temperature.isNumber() && rain.isNumber()) {
                values.add(new DailyWeather(
                        LocalDate.parse(dates.get(i).asText()),
                        temperature.asDouble(),
                        rain.asDouble()
                ));
            }
        }
        return List.copyOf(values);
    }

    private String format(double number) {
        return String.format(Locale.US, "%.4f", number);
    }
}
