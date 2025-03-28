package io.dagger.modules.reddit;

import com.google.gson.*;
import io.dagger.modules.reddit.models.Post;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

public class RedditPostFetcher {

  public static List<Post> fetchTopPosts(String subreddit, String token, String username)
      throws Exception {
    String url = "https://oauth.reddit.com/r/" + subreddit + "/top?t=day&limit=10";

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(new URI(url))
            .header("Authorization", "Bearer " + token)
            .header("User-Agent", "java:RedditMonitor:v1.0 (by /u/" + username + ")")
            .GET()
            .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    List<Post> posts = new ArrayList<>();
    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
    JsonArray children = json.getAsJsonObject("data").getAsJsonArray("children");

    for (JsonElement child : children) {
      JsonObject data = child.getAsJsonObject().getAsJsonObject("data");
      Post post = new Post();
      post.id = data.get("id").getAsString();
      post.title = data.get("title").getAsString();
      post.author = data.get("author").getAsString();
      post.score = data.get("score").getAsInt();
      post.permalink = data.get("permalink").getAsString();
      post.numComments = data.get("num_comments").getAsInt();
      post.body = data.has("selftext") ? data.get("selftext").getAsString() : "";
      posts.add(post);
    }

    return posts;
  }
}
