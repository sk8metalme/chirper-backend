package com.chirper.infrastructure.persistence.repository;

import com.chirper.infrastructure.persistence.entity.TweetJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * SpringDataTweetRepository
 * Spring Data JPAによるTweetJpaEntityのリポジトリインターフェース
 */
@Repository
public interface SpringDataTweetRepository extends JpaRepository<TweetJpaEntity, UUID> {

    /**
     * 複数のユーザーIDに基づいてツイートを取得（タイムライン用）
     * N+1クエリ問題を回避するため、効率的なクエリを使用
     * 論理削除されたツイート(isDeleted=true)は除外
     * @param userIds ユーザーIDのリスト
     * @param pageable ページネーション情報
     * @return ツイートのリスト（作成日時降順）
     */
    @Query("SELECT t FROM TweetJpaEntity t " +
           "WHERE t.userId IN :userIds " +
           "AND t.isDeleted = false " +
           "ORDER BY t.createdAt DESC")
    List<TweetJpaEntity> findByUserIdInAndIsDeletedFalse(
        @Param("userIds") List<UUID> userIds,
        Pageable pageable
    );

    /**
     * キーワードでツイートを検索（content部分一致、論理削除除外）
     * @param keyword 検索キーワード
     * @param pageable ページング情報
     * @return ツイートのリスト
     */
    @Query("SELECT t FROM TweetJpaEntity t WHERE " +
           "t.isDeleted = false AND " +
           "LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY t.createdAt DESC")
    List<TweetJpaEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * キーワード検索のヒット件数を取得
     * @param keyword 検索キーワード
     * @return 件数
     */
    @Query("SELECT COUNT(t) FROM TweetJpaEntity t WHERE " +
           "t.isDeleted = false AND " +
           "LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    long countByKeyword(@Param("keyword") String keyword);

    /**
     * 指定ユーザーのツイートを取得（ユーザープロフィール用）
     * 論理削除されたツイート(isDeleted=true)は除外
     * @param userId ユーザーID
     * @param pageable ページング情報
     * @return ツイートのリスト（作成日時降順）
     */
    @Query("SELECT t FROM TweetJpaEntity t WHERE " +
           "t.userId = :userId AND " +
           "t.isDeleted = false " +
           "ORDER BY t.createdAt DESC")
    List<TweetJpaEntity> findByUserIdAndIsDeletedFalse(@Param("userId") UUID userId, Pageable pageable);
}
