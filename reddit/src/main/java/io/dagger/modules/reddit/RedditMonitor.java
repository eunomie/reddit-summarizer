package io.dagger.modules.reddit;

import io.dagger.modules.reddit.models.Comment;
import io.dagger.modules.reddit.models.Post;
import java.util.List;

public class RedditMonitor {

  private final String clientId;
  private final String clientSecret;
  private final String username;
  private final String password;
  private String token;

  public RedditMonitor(String clientId, String clientSecret, String username, String password) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.username = username;
    this.password = password;
  }

  public void authenticate() throws Exception {
    token = RedditAuthenticator.getAccessToken(clientId, clientSecret, username, password);
  }

  public List<Post> getRecentPosts(String subreddit) throws Exception {
    return RedditPostFetcher.fetchTopPosts(subreddit, token, username);
  }

  public List<Comment> getCommentsForPost(String postId) throws Exception {
    return RedditCommentFetcher.fetchComments(postId, token, username);
  }
}
