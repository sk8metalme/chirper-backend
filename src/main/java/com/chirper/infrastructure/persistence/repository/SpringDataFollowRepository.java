package com.chirper.infrastructure.persistence.repository;

import com.chirper.infrastructure.persistence.entity.FollowJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SpringDataFollowRepository
 * Spring Data JPAによるFollowJpaEntityのリポジトリインターフェース
 */
@Repository
public interface SpringDataFollowRepository extends JpaRepository<FollowJpaEntity, UUID> {

    /**
     * フォローしているユーザーIDのリストを取得
     * @param followerUserId フォローするユーザーのID
     * @return フォローされているユーザーIDのリスト
     */
    @Query("SELECT f.followedUserId FROM FollowJpaEntity f WHERE f.followerUserId = :followerUserId")
    List<UUID> findFollowedUserIdsByFollowerUserId(@Param("followerUserId") UUID followerUserId);

    /**
     * フォロー関係を検索
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     * @return 見つかった場合はFollowJpaEntity
     */
    Optional<FollowJpaEntity> findByFollowerUserIdAndFollowedUserId(UUID followerUserId, UUID followedUserId);

    /**
     * フォロー関係を削除
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     */
    void deleteByFollowerUserIdAndFollowedUserId(UUID followerUserId, UUID followedUserId);

    /**
     * 指定ユーザーのフォロワー数を取得
     * @param followedUserId フォローされているユーザーのID
     * @return フォロワー数
     */
    long countByFollowedUserId(UUID followedUserId);

    /**
     * 指定ユーザーのフォロー数を取得
     * @param followerUserId フォローしているユーザーのID
     * @return フォロー数
     */
    long countByFollowerUserId(UUID followerUserId);

    /**
     * フォロー関係が存在するかチェック
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     * @return フォロー関係が存在する場合true
     */
    boolean existsByFollowerUserIdAndFollowedUserId(UUID followerUserId, UUID followedUserId);
}
