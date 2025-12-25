package com.chirper.presentation.dto.tweet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTweetRequest(
    @NotBlank(message = "ツイート本文は必須です")
    @Size(max = 280, message = "ツイート本文は280文字以下である必要があります")
    String content
) {}
