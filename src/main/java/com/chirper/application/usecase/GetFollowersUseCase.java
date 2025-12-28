package com.chirper.application.usecase;

import com.chirper.domain.entity.User;
import com.chirper.domain.exception.EntityNotFoundException;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GetFollowersUseCase
 * フォロワー一覧取得ユースケース
 *
 * 責務:
 * - 指定されたユーザー名のフォロワー一覧を取得
 * - ページネーション対応
 * - 現在のユーザーがフォローしているかの状態を含める
 * - N+1クエリを回避（バッチ取得）
 */
@Service
@Transactional(readOnly = true)
public class GetFollowersUseCase {

    private final IUserRepository userRepository;
    private final IFollowRepository followRepository;

    public GetFollowersUseCase(
        IUserRepository userRepository,
        IFollowRepository followRepository
    ) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    /**
     * フォロワー一覧取得を実行
     *
     * @param username 対象ユーザーのユーザー名
     * @param currentUserId 現在のユーザーID（フォロー状態確認用、nullの場合は未ログイン）
     * @param page ページ番号（0始まり）
     * @param size ページサイズ
     * @return フォロワー一覧結果
     * @throws NullPointerException usernameがnullの場合
     * @throws EntityNotFoundException ユーザーが見つからない場合
     */
    public FollowersResult execute(Username username, UserId currentUserId, int page, int size) {
        if (username == null) {
            throw new NullPointerException("Username cannot be null");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Size must not exceed 100");
        }

        // 対象ユーザーを取得
        User targetUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + username.value()));

        // フォロワーの総数を取得（ページネーション用）
        long totalFollowers = followRepository.countFollowers(targetUser.getId());
        int totalPages = totalFollowers == 0 ? 0 : (int) Math.ceil((double) totalFollowers / size);

        // ページネーションでフォロワーのUserIdリストを取得
        int offset = page * size;
        List<UserId> followerUserIds = followRepository.findFollowerUserIds(targetUser.getId(), offset, size);

        // フォロワーがいない場合は空のリストを返す
        if (followerUserIds.isEmpty()) {
            return new FollowersResult(List.of(), page, totalPages);
        }

        // ユーザー情報をバッチ取得（N+1クエリ回避）
        Map<UserId, User> followerUsersMap = userRepository.findByIds(followerUserIds);

        // 現在のユーザーがフォローしているユーザーIDのセットを取得（認証時のみ）
        Set<UserId> followedByCurrentUserIds = Set.of();
        if (currentUserId != null) {
            followedByCurrentUserIds = followRepository
                .findFollowedUserIdsIn(currentUserId, followerUserIds)
                .stream()
                .collect(Collectors.toSet());
        }

        // 結果をマッピング（元の順序を保持）
        Set<UserId> finalFollowedByCurrentUserIds = followedByCurrentUserIds;
        List<FollowerUserInfo> followerUsers = followerUserIds.stream()
            .map(followerUsersMap::get)
            .filter(user -> user != null)  // 念のため存在チェック
            .map(user -> new FollowerUserInfo(
                user,
                finalFollowedByCurrentUserIds.contains(user.getId())
            ))
            .collect(Collectors.toList());

        return new FollowersResult(followerUsers, page, totalPages);
    }

    /**
     * フォロワーユーザー情報
     *
     * @param user ユーザーエンティティ
     * @param followedByCurrentUser 現在のユーザーがフォローしているか
     */
    public record FollowerUserInfo(
        User user,
        boolean followedByCurrentUser
    ) {}

    /**
     * フォロワー一覧結果
     *
     * @param followers フォロワーユーザーのリスト
     * @param currentPage 現在のページ番号
     * @param totalPages 総ページ数
     */
    public record FollowersResult(
        List<FollowerUserInfo> followers,
        int currentPage,
        int totalPages
    ) {}
}
