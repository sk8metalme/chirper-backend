package com.chirper.infrastructure.persistence.repository;

import com.chirper.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SpringDataUserRepository
 * Spring Data JPAによるUserJpaEntityのリポジトリインターフェース
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    /**
     * ユーザー名で検索
     * @param username ユーザー名
     * @return 見つかった場合はUserJpaEntity
     */
    Optional<UserJpaEntity> findByUsername(String username);

    /**
     * メールアドレスで検索
     * @param email メールアドレス
     * @return 見つかった場合はUserJpaEntity
     */
    Optional<UserJpaEntity> findByEmail(String email);

    /**
     * キーワードでユーザーを検索（username, displayNameで部分一致）
     * @param keyword 検索キーワード
     * @param pageable ページング情報
     * @return ユーザーのリスト
     */
    @Query("SELECT u FROM UserJpaEntity u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UserJpaEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * キーワード検索のヒット件数を取得
     * @param keyword 検索キーワード
     * @return 件数
     */
    @Query("SELECT COUNT(u) FROM UserJpaEntity u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    long countByKeyword(@Param("keyword") String keyword);

    /**
     * 複数のユーザーIDでユーザーをバッチ取得（N+1クエリ回避）
     * Spring Data JPAの組み込みメソッドfindAllById()を使用
     * @param userIds ユーザーIDのリスト
     * @return ユーザーのリスト
     */
    List<UserJpaEntity> findAllById(Iterable<UUID> userIds);
}
