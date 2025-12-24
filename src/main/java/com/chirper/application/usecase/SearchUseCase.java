package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.repository.IUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SearchUseCase
 * ユーザーとツイートの検索ユースケース
 *
 * 責務:
 * - キーワードによるユーザー検索
 * - キーワードによるツイート検索
 * - 検索結果の集約と返却
 *
 * Note: Phase 5で実装予定。現在は空の結果を返すプレースホルダー実装
 */
@Service
@Transactional(readOnly = true)
public class SearchUseCase {

    // Phase 5で使用予定のリポジトリ
    private final IUserRepository userRepository;
    private final ITweetRepository tweetRepository;

    public SearchUseCase(IUserRepository userRepository, ITweetRepository tweetRepository) {
        this.userRepository = userRepository;
        this.tweetRepository = tweetRepository;
    }

    /**
     * キーワードでユーザーとツイートを検索
     * @param keyword 検索キーワード（2文字以上）
     * @return 検索結果（ユーザーとツイートのリスト）
     * @throws IllegalArgumentException キーワードがnull、空、または2文字未満の場合
     */
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
