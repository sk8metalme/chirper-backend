package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.exception.InvalidOperationException;
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
     * @param page ページ番号（0始まり）
     * @param size ページサイズ
     * @return 検索結果（ユーザーとツイートのリスト）
     * @throws InvalidOperationException キーワードがnull、空、または2文字未満の場合
     */
    public SearchResult execute(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new InvalidOperationException("検索キーワードを入力してください");
        }

        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.length() < 2) {
            throw new InvalidOperationException("検索キーワードは2文字以上で入力してください");
        }

        // 実際の検索実行
        List<User> users = userRepository.searchByKeyword(trimmedKeyword, page, size);
        List<Tweet> tweets = tweetRepository.searchByKeyword(trimmedKeyword, page, size);

        return new SearchResult(users, tweets);
    }

    public record SearchResult(List<User> users, List<Tweet> tweets) {}
}
