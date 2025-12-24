package com.chirper.domain.repository;

import com.chirper.domain.entity.User;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;

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
}
