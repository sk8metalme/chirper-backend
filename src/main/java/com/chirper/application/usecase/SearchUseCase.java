package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.repository.IUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchUseCase {

    private final IUserRepository userRepository;
    private final ITweetRepository tweetRepository;

    public SearchUseCase(IUserRepository userRepository, ITweetRepository tweetRepository) {
        this.userRepository = userRepository;
        this.tweetRepository = tweetRepository;
    }

    public SearchResult execute(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be null or empty");
        }

        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.length() < 2) {
            throw new IllegalArgumentException("Search keyword must be at least 2 characters");
        }

        // TODO: Phase 5で実装予定
        // IUserRepository.searchByKeyword(String keyword, int page, int size)
        // ITweetRepository.searchByKeyword(String keyword, int page, int size)
        // の実装が必要

        return new SearchResult(List.of(), List.of());
    }

    public record SearchResult(List<User> users, List<Tweet> tweets) {}
}
