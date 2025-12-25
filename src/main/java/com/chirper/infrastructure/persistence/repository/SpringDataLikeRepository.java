package com.chirper.infrastructure.persistence.repository;

import com.chirper.infrastructure.persistence.entity.LikeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * 指定ツイートのいいね数を取得
     * @param tweetId ツイートID
     * @return いいね数
     */
    long countByTweetId(UUID tweetId);

    /**
     * 複数ツイートのいいね数をバッチ取得（N+1クエリ回避）
     * GROUP BYを使用して一括取得
     * @param tweetIds ツイートIDのリスト
     * @return ツイートIDといいね数のリスト（Object[]形式）
     */
    @Query("SELECT l.tweetId, COUNT(l) FROM LikeJpaEntity l WHERE l.tweetId IN :tweetIds GROUP BY l.tweetId")
    List<Object[]> countByTweetIds(@Param("tweetIds") List<UUID> tweetIds);

    /**
     * 指定ユーザーがいいねしたツイートIDリストを取得（N+1クエリ回避）
     * @param userId ユーザーID
     * @return いいねしたツイートIDのリスト
     */
    @Query("SELECT l.tweetId FROM LikeJpaEntity l WHERE l.userId = :userId")
    List<UUID> findTweetIdsByUserId(@Param("userId") UUID userId);
}
