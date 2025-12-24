package com.chirper.domain.repository;

import com.chirper.domain.entity.Follow;
import com.chirper.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * IFollowRepository Interface
 * フォロー関係エンティティの永続化を抽象化するリポジトリインターフェース
 * Domain層で定義し、Infrastructure層で実装（依存性逆転の原則）
 */
public interface IFollowRepository {

    /**
     * フォロー関係を保存
     * @param follow 保存するFollowエンティティ
     * @return 保存されたFollowエンティティ
     */
    Follow save(Follow follow);

    /**
     * 指定ユーザーがフォローしているユーザーIDのリストを取得
     * @param followerUserId フォローしているユーザーのID
     * @return フォローされているユーザーIDのリスト
     */
    List<UserId> findFollowedUserIds(UserId followerUserId);

    /**
     * フォロー関係を検索
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     * @return 見つかった場合はFollowエンティティ、見つからない場合はOptional.empty()
     */
    Optional<Follow> findByFollowerAndFollowed(UserId followerUserId, UserId followedUserId);

    /**
     * フォロー関係を削除
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     */
    void delete(UserId followerUserId, UserId followedUserId);
}
