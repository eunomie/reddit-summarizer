package io.dagger.modules.reddit;

import io.dagger.client.Secret;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;
import java.util.List;

@Object
public class Reddit {

  @Object
  public static class Post {
    public String postId;
    public String title;
    public String author;
    public String body;
    public int numComments;
  }

  @Object
  public static class Comment {
    public String commentId;
    public String body;
    public String author;
    public int score;
    public String parentId;
  }

  private Secret clientId;
  private Secret clientSecret;
  private Secret username;
  private Secret password;

  public Reddit() {}

  public Reddit(Secret clientId, Secret clientSecret, Secret username, Secret password) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.username = username;
    this.password = password;
  }

  /**
   * Get the recent posts from a subreddit
   *
   * @param subreddit the subreddit to get posts from
   */
  @Function
  public List<String> posts(String subreddit) throws Exception {
    return getPosts(subreddit).stream().map(p -> """
# %s

written by %s
comments: %d

%s
        """.formatted(p.title, p.author, p.numComments, p.body)).toList();
  }

  public List<Post> getPosts(String subreddit) throws Exception {
    RedditMonitor monitor =
        new RedditMonitor(
            clientId.plaintext(),
            clientSecret.plaintext(),
            username.plaintext(),
            password.plaintext());
    monitor.authenticate();

    return monitor.getRecentPosts(subreddit).stream()
        .map(
            p -> {
              var post = new Post();
              post.postId = p.id;
              post.title = p.title;
              post.author = p.author;
              post.body = p.body;
              post.numComments = p.numComments;
              return post;
            })
        .toList();
  }
}
