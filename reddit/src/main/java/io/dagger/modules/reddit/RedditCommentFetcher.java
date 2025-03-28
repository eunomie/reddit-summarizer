package io.dagger.modules.reddit;

import com.google.gson.*;
import io.dagger.modules.reddit.models.Comment;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

public class RedditCommentFetcher {

  public static List<Comment> fetchComments(String postId, String token, String username) throws Exception {
    String url = "https://oauth.reddit.com/comments/" + postId + "?limit=500";

    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(url))
        .header("Authorization", "Bearer " + token)
        .header("User-Agent", "java:RedditMonitor:v1.0 (by /u/" + username + ")")
        .GET()
        .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    List<Comment> comments = new ArrayList<>();
    JsonArray root = JsonParser.parseString(response.body()).getAsJsonArray();

    // Comments are in the second element
    JsonArray commentChildren = root.get(1).getAsJsonObject().getAsJsonObject("data").getAsJsonArray("children");

    for (JsonElement child : commentChildren) {
      if (child.getAsJsonObject().get("kind").getAsString().equals("t1")) { // Ensure it's a comment
        collectCommentRecursively(child.getAsJsonObject().getAsJsonObject("data"), comments);
      }
    }

    return comments;
  }

  private static void collectCommentRecursively(JsonObject data, List<Comment> comments) {
    Comment comment = new Comment();
    comment.id = data.get("id").getAsString();
    comment.body = data.has("body") ? data.get("body").getAsString() : "";
    comment.author = data.get("author").getAsString();
    comment.score = data.get("score").getAsInt();
    comment.parentId = data.get("parent_id").getAsString();
    comments.add(comment);

    if (data.has("replies") && data.get("replies").isJsonObject()) {
      JsonObject replies = data.getAsJsonObject("replies").getAsJsonObject("data");
      JsonArray children = replies.getAsJsonArray("children");
      for (JsonElement replyChild : children) {
        if (replyChild.getAsJsonObject().get("kind").getAsString().equals("t1")) {
          collectCommentRecursively(replyChild.getAsJsonObject().getAsJsonObject("data"), comments);
        }
      }
    }
  }
}
