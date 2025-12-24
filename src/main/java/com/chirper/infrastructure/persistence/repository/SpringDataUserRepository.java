package com.chirper.infrastructure.persistence.repository;

import com.chirper.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
