package io.dagger.modules.reddit.models;

import java.util.List;

public class Post {
  public String id;
  public String title;
  public String author;
  public int score;
  public String permalink;
  public int numComments;
  public String body;
  public List<Comment> comments;
}
