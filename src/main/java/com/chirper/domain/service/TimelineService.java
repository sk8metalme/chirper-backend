package com.chirper.domain.service;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.UserId;

import java.util.List;
import java.util.Objects;

/**
 * TimelineService
 * タイムライン生成に関するドメインサービス
 * N+1クエリ問題を回避した効率的なタイムライン取得を担当
 */
public class TimelineService {

    private final ITweetRepository tweetRepository;

    /**
     * コンストラクタ
     * @param tweetRepository ツイートリポジトリ
     */
    public TimelineService(ITweetRepository tweetRepository) {
        this.tweetRepository = Objects.requireNonNull(tweetRepository, "TweetRepository cannot be null");
    }

    /**
     * タイムライン取得
     * フォローユーザーのツイートを時系列で取得
     * N+1クエリ問題を回避するため、ITweetRepository.findByUserIdsWithDetails()を使用
     * @param followedUserIds フォローしているユーザーIDのリスト
     * @param page ページ番号（0始まり）
     * @param size ページサイズ
     * @return ツイートのリスト（作成日時降順）
     */
    public List<Tweet> getTimeline(List<UserId> followedUserIds, int page, int size) {
        if (followedUserIds == null || followedUserIds.isEmpty()) {
            return List.of();
        }

        if (page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        return tweetRepository.findByUserIdsWithDetails(followedUserIds, page, size);
    }

    /**
     * タイムラインの総ページ数を計算
     * @param followedUserIds フォローしているユーザーIDのリスト
     * @param size ページサイズ
     * @return 総ページ数
     */
    public int calculateTotalPages(List<UserId> followedUserIds, int size) {
        if (followedUserIds == null || followedUserIds.isEmpty()) {
            return 0;
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        // TODO: Phase 4で実装予定
        // リポジトリにカウントメソッドを追加する必要があります
        // ITweetRepository.countByUserIds(List<UserId> userIds) の実装が必要
        throw new UnsupportedOperationException(
            "calculateTotalPages is not implemented yet. " +
            "ITweetRepository.countByUserIds() method needs to be added in Phase 4.");
    }
}
