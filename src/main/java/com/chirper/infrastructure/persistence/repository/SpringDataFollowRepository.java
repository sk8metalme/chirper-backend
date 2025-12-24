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
}
