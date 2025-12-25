package com.chirper.presentation.dto.tweet;

import java.util.List;

public record TimelineResponse(
    List<TweetResponse> tweets,
    int totalPages
) {}
