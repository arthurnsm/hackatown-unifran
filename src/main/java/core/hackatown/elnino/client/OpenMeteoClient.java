package br.com.hackatown.elnino.client;

import br.com.hackatown.elnino.model.DailyWeather;
import br.com.hackatown.elnino.model.Location;
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
        String latitudes = locations.stream()
                .map(location -> format(location.latitude()))
                .collect(Collectors.joining(","));
        String longitudes = locations.stream()
                .map(location -> format(location.longitude()))
                .collect(Collectors.joining(","));

        String url = ARCHIVE_URL
                + "?latitude=" + latitudes
                + "&longitude=" + longitudes
                + "&start_date=" + start
                + "&end_date=" + end
                + "&daily=temperature_2m_mean,precipitation_sum"
                + "&models=era5"
                + "&timezone=GMT";

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(45))
                .header("Accept", "application/json")
                .header("User-Agent", "Hackatown-El-Nino/1.0")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Open-Meteo respondeu HTTP " + response.statusCode());
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
