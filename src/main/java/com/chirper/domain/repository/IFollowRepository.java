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

    /**
     * 指定ユーザーのフォロワー数を取得
     * @param userId フォローされているユーザーのID
     * @return フォロワー数
     */
    long countFollowers(UserId userId);

    /**
     * 指定ユーザーのフォロー数を取得
     * @param userId フォローしているユーザーのID
     * @return フォロー数
     */
    long countFollowing(UserId userId);

    /**
     * フォロー関係が存在するかチェック
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     * @return フォロー関係が存在する場合true
     */
    boolean existsByFollowerAndFollowed(UserId followerUserId, UserId followedUserId);

    /**
     * 指定ユーザーのフォロワーのUserIdリストを取得（ページネーション対応）
     * @param followedUserId フォローされているユーザーのID
     * @param offset 取得開始位置（0から始まる）
     * @param limit 取得件数
     * @return フォロワーのUserIdリスト
     */
    List<UserId> findFollowerUserIds(UserId followedUserId, int offset, int limit);

    /**
     * 指定ユーザーがフォローしているユーザーのUserIdリストを取得（ページネーション対応）
     * @param followerUserId フォローしているユーザーのID
     * @param offset 取得開始位置（0から始まる）
     * @param limit 取得件数
     * @return フォローしているユーザーのUserIdリスト
     */
    List<UserId> findFollowingUserIds(UserId followerUserId, int offset, int limit);

    /**
     * 指定したUserIdリストの中で、currentUserがフォローしているUserIdを取得（バッチ処理用）
     * @param followerUserId フォローしているユーザーのID
     * @param targetUserIds 対象となるUserIdのリスト
     * @return followerUserIdがフォローしているUserIdのリスト
     */
    List<UserId> findFollowedUserIdsIn(UserId followerUserId, List<UserId> targetUserIds);
}
