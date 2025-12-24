package com.chirper.domain.repository;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * ITweetRepository Interface
 * ツイートエンティティの永続化を抽象化するリポジトリインターフェース
 * Domain層で定義し、Infrastructure層で実装（依存性逆転の原則）
 */
public interface ITweetRepository {

    /**
     * ツイートを保存または更新
     * @param tweet 保存するTweetエンティティ
     * @return 保存されたTweetエンティティ
     */
    Tweet save(Tweet tweet);

    /**
     * IDでツイートを検索
     * @param tweetId ツイートID
     * @return 見つかった場合はTweetエンティティ、見つからない場合はOptional.empty()
     */
    Optional<Tweet> findById(TweetId tweetId);

    /**
     * 複数のユーザーIDに基づいてツイートを取得（タイムライン用）
     * N+1クエリ問題を回避するため、JOIN FETCHまたは@EntityGraphを使用
     * 論理削除されたツイート(isDeleted=true)は除外
     * @param userIds ユーザーIDのリスト
     * @param page ページ番号（0始まり）
     * @param size ページサイズ
     * @return ツイートのリスト（作成日時降順）
     */
    List<Tweet> findByUserIdsWithDetails(List<UserId> userIds, int page, int size);

    /**
     * ツイートを削除
     * @param tweetId 削除するツイートのID
     */
    void delete(TweetId tweetId);
}
