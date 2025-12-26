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
 * GetFollowingUseCase
 * フォロー中一覧取得ユースケース
 *
 * 責務:
 * - 指定されたユーザー名のフォロー中ユーザー一覧を取得
 * - ページネーション対応
 * - 現在のユーザーがフォローしているかの状態を含める
 * - N+1クエリを回避（バッチ取得）
 */
@Service
@Transactional(readOnly = true)
public class GetFollowingUseCase {

    private final IUserRepository userRepository;
    private final IFollowRepository followRepository;

    public GetFollowingUseCase(
        IUserRepository userRepository,
        IFollowRepository followRepository
    ) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    /**
     * フォロー中一覧取得を実行
     *
     * @param username 対象ユーザーのユーザー名
     * @param currentUserId 現在のユーザーID（フォロー状態確認用、nullの場合は未ログイン）
     * @param page ページ番号（0始まり）
     * @param size ページサイズ
     * @return フォロー中一覧結果
     * @throws NullPointerException usernameがnullの場合
     * @throws EntityNotFoundException ユーザーが見つからない場合
     */
    public FollowingResult execute(Username username, UserId currentUserId, int page, int size) {
        if (username == null) {
            throw new NullPointerException("Username cannot be null");
        }

        // 対象ユーザーを取得
        User targetUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + username.value()));

        // フォロー中の総数を取得（ページネーション用）
        long totalFollowing = followRepository.countFollowing(targetUser.getId());
        int totalPages = (int) Math.ceil((double) totalFollowing / size);

        // ページネーションでフォロー中のUserIdリストを取得
        int offset = page * size;
        List<UserId> followingUserIds = followRepository.findFollowingUserIds(targetUser.getId(), offset, size);

        // フォロー中のユーザーがいない場合は空のリストを返す
        if (followingUserIds.isEmpty()) {
            return new FollowingResult(List.of(), page, totalPages);
        }

        // ユーザー情報をバッチ取得（N+1クエリ回避）
        Map<UserId, User> followingUsersMap = userRepository.findByIds(followingUserIds);

        // 現在のユーザーがフォローしているユーザーIDのセットを取得（認証時のみ）
        Set<UserId> followedByCurrentUserIds = Set.of();
        if (currentUserId != null) {
            followedByCurrentUserIds = followRepository
                .findFollowedUserIdsIn(currentUserId, followingUserIds)
                .stream()
                .collect(Collectors.toSet());
        }

        // 結果をマッピング（元の順序を保持）
        Set<UserId> finalFollowedByCurrentUserIds = followedByCurrentUserIds;
        List<FollowingUserInfo> followingUsers = followingUserIds.stream()
            .map(followingUsersMap::get)
            .filter(user -> user != null)  // 念のため存在チェック
            .map(user -> new FollowingUserInfo(
                user,
                finalFollowedByCurrentUserIds.contains(user.getId())
            ))
            .collect(Collectors.toList());

        return new FollowingResult(followingUsers, page, totalPages);
    }

    /**
     * フォロー中ユーザー情報
     *
     * @param user ユーザーエンティティ
     * @param followedByCurrentUser 現在のユーザーがフォローしているか
     */
    public record FollowingUserInfo(
        User user,
        boolean followedByCurrentUser
    ) {}

    /**
     * フォロー中一覧結果
     *
     * @param following フォロー中ユーザーのリスト
     * @param currentPage 現在のページ番号
     * @param totalPages 総ページ数
     */
    public record FollowingResult(
        List<FollowingUserInfo> following,
        int currentPage,
        int totalPages
    ) {}
}
