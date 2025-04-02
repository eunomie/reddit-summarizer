package main

import (
	"context"

	"dagger/reddit-summarizer/internal/dagger"
)

type RedditSummarizer struct {
	ClientId     *dagger.Secret
	ClientSecret *dagger.Secret
	Username     *dagger.Secret
	Password     *dagger.Secret
}

func New(clientId, clientSecret, username, password *dagger.Secret) *RedditSummarizer {
	return &RedditSummarizer{
		ClientId:     clientId,
		ClientSecret: clientSecret,
		Username:     username,
		Password:     password,
	}
}

// Summarize a subreddit
func (r *RedditSummarizer) Summarize(
	ctx context.Context,
	// Name of the subreddit to summarize
	subreddit string,
) (string, error) {
	// Define the environment for the LLM
	redditEnvironment := dag.Env().
		// Create a reddit fetcher. This is a module written in Java
		WithRedditInput("redditFetcher", dag.Reddit(r.ClientId, r.ClientSecret, r.Username, r.Password), "Reddit fetcher to read posts from a subreddit")
		// Specify the subrredit to fetch
		//WithStringInput("subreddit", subreddit, "Subreddit to summarize")

	return dag.
		// Use LLM
		LLM().
		// Make the reddit fetcher available for the LLM
		WithEnv(redditEnvironment).
		// Ask to generate the summary
		WithPrompt(`**Role**: Reddit Summarizer

**Task**: Create a markdown summary of the subreddit 'docker'

**Environment**:

You have access to a reddit fetcher.
Use the available 'posts' tool to get the posts from the subreddit.

**Execution Rule**:
- **Always run the posts tool**.

**Instructions**:

1. Use the 'posts' tool to get the posts from the subreddit.
2. Summarize the subreddit posts in a markdown format.

Format the response in markdown
`).
		LastReply(ctx)
}

// Print a summary of the subreddit
func (r *RedditSummarizer) PrintSummary(
	ctx context.Context,
	// Name of the subreddit to summarize
	subreddit string,
) (string, error) {
	// Retrieve the summary using the LLM
	summary, err := r.Summarize(ctx, subreddit)
	if err != nil {
		return "", err
	}

	// Use module glow to display the markdown nicely
	return dag.Glow().DisplayMarkdown(ctx, summary)
}
