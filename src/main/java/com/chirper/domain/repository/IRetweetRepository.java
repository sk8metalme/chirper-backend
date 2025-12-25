package com.chirper.domain.repository;

import com.chirper.domain.entity.Retweet;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * IRetweetRepository Interface
 * リツイートエンティティの永続化を抽象化するリポジトリインターフェース
 * Domain層で定義し、Infrastructure層で実装（依存性逆転の原則）
 */
public interface IRetweetRepository {

    /**
     * リツイートを保存
     * @param retweet 保存するRetweetエンティティ
     * @return 保存されたRetweetエンティティ
     */
    Retweet save(Retweet retweet);

    /**
     * ツイートIDでリツイートを検索
     * @param tweetId ツイートID
     * @return リツイートのリスト
     */
    List<Retweet> findByTweetId(TweetId tweetId);

    /**
     * ユーザーIDでリツイートを検索
     * @param userId ユーザーID
     * @return リツイートのリスト
     */
    List<Retweet> findByUserId(UserId userId);

    /**
     * 特定のユーザーが特定のツイートをリツイートしているか検索
     * @param userId ユーザーID
     * @param tweetId ツイートID
     * @return 見つかった場合はRetweetエンティティ、見つからない場合はOptional.empty()
     */
    Optional<Retweet> findByUserIdAndTweetId(UserId userId, TweetId tweetId);

    /**
     * リツイートを削除
     * @param userId ユーザーID
     * @param tweetId ツイートID
     */
    void delete(UserId userId, TweetId tweetId);

    /**
     * 指定ツイートのリツイート数を取得
     * @param tweetId ツイートID
     * @return リツイート数
     */
    long countByTweetId(TweetId tweetId);

    /**
     * 複数ツイートのリツイート数をバッチ取得（N+1クエリ回避）
     * @param tweetIds ツイートIDのリスト
     * @return ツイートIDとリツイート数のマップ
     */
    Map<TweetId, Long> countByTweetIds(List<TweetId> tweetIds);

    /**
     * 指定ユーザーがリツイートしたツイートIDリストを取得（N+1クエリ回避）
     * @param userId ユーザーID
     * @return リツイートしたツイートIDのリスト
     */
    List<TweetId> findTweetIdsByUserId(UserId userId);
}
