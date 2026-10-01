package br.com.hackatown.elnino.alerts.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Sends SMS messages through Twilio's REST API. */
public class TwilioSmsClient {
    private static final URI API_BASE_URL = URI.create("https://api.twilio.com/2010-04-01/Accounts/");
    private static final Pattern MESSAGE_SID = Pattern.compile("\\\"sid\\\"\\s*:\\s*\\\"(SM[a-fA-F0-9]{32})\\\"");

    private final HttpClient httpClient;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    private TwilioSmsClient(HttpClient httpClient, String accountSid, String authToken, String fromNumber) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.accountSid = required("TWILIO_ACCOUNT_SID", accountSid);
        this.authToken = required("TWILIO_AUTH_TOKEN", authToken);
        this.fromNumber = required("TWILIO_FROM_NUMBER", fromNumber);
    }

    public static TwilioSmsClient fromEnvironment() {
        return new TwilioSmsClient(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                System.getenv("TWILIO_ACCOUNT_SID"),
                System.getenv("TWILIO_AUTH_TOKEN"),
                System.getenv("TWILIO_FROM_NUMBER"));
    }

    public String send(String toNumber, String body) {
        HttpRequest request = HttpRequest.newBuilder(messageEndpoint())
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", authorizationHeader())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody(toNumber, body)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Twilio rejected the SMS request (HTTP " + response.statusCode() + ").");
            }
            return extractMessageSid(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not reach Twilio to send the SMS.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Twilio SMS request was interrupted.", exception);
        }
    }

    private URI messageEndpoint() {
        return API_BASE_URL.resolve(accountSid + "/Messages.json");
    }

    private String authorizationHeader() {
        String credentials = accountSid + ":" + authToken;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private String formBody(String toNumber, String body) {
        return "From=" + encode(fromNumber)
                + "&To=" + encode(toNumber)
                + "&Body=" + encode(body);
    }

    private String extractMessageSid(String responseBody) {
        Matcher matcher = MESSAGE_SID.matcher(responseBody);
        if (!matcher.find()) {
            throw new IllegalStateException("Twilio accepted the SMS request but did not return a message SID.");
        }
        return matcher.group(1);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String required(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + name + " is required.");
        }
        return value;
    }
}
