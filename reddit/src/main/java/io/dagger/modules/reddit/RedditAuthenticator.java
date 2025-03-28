package io.dagger.modules.reddit;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RedditAuthenticator {

  public static String getAccessToken(String clientId, String clientSecret, String username, String password) throws Exception {
    String auth = clientId + ":" + clientSecret;
    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

    String body = "grant_type=password&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
        "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI("https://www.reddit.com/api/v1/access_token"))
        .header("Authorization", "Basic " + encodedAuth)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("User-Agent", "java:RedditMonitor:v1.0 (by /u/" + username + ")")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    String token = new com.google.gson.JsonParser()
        .parse(response.body())
        .getAsJsonObject()
        .get("access_token")
        .getAsString();

    return token;
  }
}
