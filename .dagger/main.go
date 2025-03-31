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
	// Create a reddit fetcher. This is a module written in Java
	redditFetcher := dag.Reddit(r.ClientId, r.ClientSecret, r.Username, r.Password)

	return dag.
		// Use LLM
		LLM().
		// Make the reddit fetcher available for the LLM
		WithReddit(redditFetcher).
		// Ask to generate the summary
		WithPromptVar("assignment", "Create a summary of the subreddit '"+subreddit+"'").
		WithPrompt(`
Task: $assignment

You have access to reddit with a tool called "posts" that can be used to get the posts to summarize.

Write a few sentences about the subreddit and highlight the most interesting posts you received.

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
