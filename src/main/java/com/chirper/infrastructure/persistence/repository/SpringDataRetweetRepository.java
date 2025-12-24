package com.chirper.infrastructure.persistence.repository;

import com.chirper.infrastructure.persistence.entity.RetweetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SpringDataRetweetRepository
 * Spring Data JPAによるRetweetJpaEntityのリポジトリインターフェース
 */
@Repository
public interface SpringDataRetweetRepository extends JpaRepository<RetweetJpaEntity, UUID> {

    /**
     * ツイートIDでリツイートを検索
     * @param tweetId ツイートID
     * @return リツイートのリスト
     */
    List<RetweetJpaEntity> findByTweetId(UUID tweetId);

    /**
     * ユーザーIDでリツイートを検索
     * @param userId ユーザーID
     * @return リツイートのリスト
     */
    List<RetweetJpaEntity> findByUserId(UUID userId);

    /**
     * 特定のユーザーが特定のツイートをリツイートしているか検索
     * @param userId ユーザーID
     * @param tweetId ツイートID
     * @return 見つかった場合はRetweetJpaEntity
     */
    Optional<RetweetJpaEntity> findByUserIdAndTweetId(UUID userId, UUID tweetId);

    /**
     * リツイートを削除
     * @param userId ユーザーID
     * @param tweetId ツイートID
     */
    void deleteByUserIdAndTweetId(UUID userId, UUID tweetId);
}
