package com.chirper.domain.repository;

import com.chirper.domain.entity.User;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * IUserRepository Interface
 * ユーザーエンティティの永続化を抽象化するリポジトリインターフェース
 * Domain層で定義し、Infrastructure層で実装（依存性逆転の原則）
 */
public interface IUserRepository {

    /**
     * ユーザーを保存または更新
     * @param user 保存するUserエンティティ
     * @return 保存されたUserエンティティ
     */
    User save(User user);

    /**
     * IDでユーザーを検索
     * @param userId ユーザーID
     * @return 見つかった場合はUserエンティティ、見つからない場合はOptional.empty()
     */
    Optional<User> findById(UserId userId);

    /**
     * ユーザー名でユーザーを検索
     * @param username ユーザー名
     * @return 見つかった場合はUserエンティティ、見つからない場合はOptional.empty()
     */
    Optional<User> findByUsername(Username username);

    /**
     * メールアドレスでユーザーを検索
     * @param email メールアドレス
     * @return 見つかった場合はUserエンティティ、見つからない場合はOptional.empty()
     */
    Optional<User> findByEmail(Email email);

    /**
     * ユーザーを削除
     * @param userId 削除するユーザーのID
     */
    void delete(UserId userId);

    /**
     * キーワードでユーザーを検索（username, displayNameで部分一致）
     * @param keyword 検索キーワード
     * @param page ページ番号（0始まり）
     * @param size ページサイズ
     * @return ユーザーのリスト
     */
    java.util.List<User> searchByKeyword(String keyword, int page, int size);

    /**
     * キーワード検索のヒット件数を取得
     * @param keyword 検索キーワード
     * @return 件数
     */
    long countByKeyword(String keyword);

    /**
     * 複数のユーザーIDでユーザーをバッチ取得（N+1クエリ回避）
     * @param userIds ユーザーIDのリスト
     * @return ユーザーIDとUserエンティティのマップ
     */
    Map<UserId, User> findByIds(List<UserId> userIds);
}
