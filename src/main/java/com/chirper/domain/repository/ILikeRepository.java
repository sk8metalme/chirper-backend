package com.chirper.domain.repository;

import com.chirper.domain.entity.Like;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * ILikeRepository Interface
 * いいねエンティティの永続化を抽象化するリポジトリインターフェース
 * Domain層で定義し、Infrastructure層で実装（依存性逆転の原則）
 */
public interface ILikeRepository {

    /**
     * いいねを保存
     * @param like 保存するLikeエンティティ
     * @return 保存されたLikeエンティティ
     */
    Like save(Like like);

    /**
     * ツイートIDでいいねを検索
     * @param tweetId ツイートID
     * @return いいねのリスト
     */
    List<Like> findByTweetId(TweetId tweetId);

    /**
     * ユーザーIDでいいねを検索
     * @param userId ユーザーID
     * @return いいねのリスト
     */
    List<Like> findByUserId(UserId userId);

    /**
     * 特定のユーザーが特定のツイートにいいねしているか検索
     * @param userId ユーザーID
     * @param tweetId ツイートID
     * @return 見つかった場合はLikeエンティティ、見つからない場合はOptional.empty()
     */
    Optional<Like> findByUserIdAndTweetId(UserId userId, TweetId tweetId);

    /**
     * いいねを削除
     * @param userId ユーザーID
     * @param tweetId ツイートID
     */
    void delete(UserId userId, TweetId tweetId);
}
