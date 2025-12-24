package com.chirper.infrastructure.persistence.repository;

import com.chirper.infrastructure.persistence.entity.LikeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SpringDataLikeRepository
 * Spring Data JPAによるLikeJpaEntityのリポジトリインターフェース
 */
@Repository
public interface SpringDataLikeRepository extends JpaRepository<LikeJpaEntity, UUID> {

    /**
     * ツイートIDでいいねを検索
     * @param tweetId ツイートID
     * @return いいねのリスト
     */
    List<LikeJpaEntity> findByTweetId(UUID tweetId);

    /**
     * ユーザーIDでいいねを検索
     * @param userId ユーザーID
     * @return いいねのリスト
     */
    List<LikeJpaEntity> findByUserId(UUID userId);

    /**
     * 特定のユーザーが特定のツイートにいいねしているか検索
     * @param userId ユーザーID
     * @param tweetId ツイートID
     * @return 見つかった場合はLikeJpaEntity
     */
    Optional<LikeJpaEntity> findByUserIdAndTweetId(UUID userId, UUID tweetId);

    /**
     * いいねを削除
     * @param userId ユーザーID
     * @param tweetId ツイートID
     */
    void deleteByUserIdAndTweetId(UUID userId, UUID tweetId);
}
