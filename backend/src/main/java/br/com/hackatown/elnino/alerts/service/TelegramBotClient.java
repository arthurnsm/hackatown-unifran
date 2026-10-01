package br.com.hackatown.elnino.alerts.service;

import br.com.hackatown.elnino.alerts.config.EnvironmentConfig;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Sends text alerts through the Telegram Bot API. */
public class TelegramBotClient {
    private static final Pattern MESSAGE_ID = Pattern.compile("\\\"message_id\\\"\\s*:\\s*(\\d+)");

    private final HttpClient httpClient;
    private final String botToken;

    private TelegramBotClient(HttpClient httpClient, String botToken) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.botToken = Objects.requireNonNull(botToken);
    }

    public static TelegramBotClient fromEnvironment() {
        return new TelegramBotClient(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                EnvironmentConfig.required("TELEGRAM_BOT_TOKEN"));
    }

    public String send(String chatId, String text) {
        HttpRequest request = HttpRequest.newBuilder(sendMessageEndpoint())
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody(chatId, text)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Telegram rejected the alert request (HTTP "
                                + response.statusCode()
                                + "): "
                                + response.body());
            }
            return extractMessageId(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not reach Telegram to send the alert.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Telegram alert request was interrupted.", exception);
        }
    }

    private URI sendMessageEndpoint() {
        return URI.create("https://api.telegram.org/bot" + botToken + "/sendMessage");
    }

    private String formBody(String chatId, String text) {
        return "chat_id=" + encode(chatId) + "&text=" + encode(text);
    }

    private String extractMessageId(String responseBody) {
        Matcher matcher = MESSAGE_ID.matcher(responseBody);
        if (!matcher.find()) {
            throw new IllegalStateException("Telegram accepted the alert request but did not return a message ID.");
        }
        return matcher.group(1);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
