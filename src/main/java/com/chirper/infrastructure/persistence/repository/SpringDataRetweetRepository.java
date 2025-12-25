package com.chirper.infrastructure.persistence.repository;

import com.chirper.infrastructure.persistence.entity.RetweetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * 指定ツイートのリツイート数を取得
     * @param tweetId ツイートID
     * @return リツイート数
     */
    long countByTweetId(UUID tweetId);

    /**
     * 複数ツイートのリツイート数をバッチ取得（N+1クエリ回避）
     * GROUP BYを使用して一括取得
     * @param tweetIds ツイートIDのリスト
     * @return ツイートIDとリツイート数のリスト（Object[]形式）
     */
    @Query("SELECT r.tweetId, COUNT(r) FROM RetweetJpaEntity r WHERE r.tweetId IN :tweetIds GROUP BY r.tweetId")
    List<Object[]> countByTweetIds(@Param("tweetIds") List<UUID> tweetIds);

    /**
     * 指定ユーザーがリツイートしたツイートIDリストを取得（N+1クエリ回避）
     * @param userId ユーザーID
     * @return リツイートしたツイートIDのリスト
     */
    @Query("SELECT r.tweetId FROM RetweetJpaEntity r WHERE r.userId = :userId")
    List<UUID> findTweetIdsByUserId(@Param("userId") UUID userId);
}
