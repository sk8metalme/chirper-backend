package com.chirper.infrastructure.persistence.repository;

import com.chirper.domain.entity.User;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import com.chirper.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * UserRepositoryImpl
 * IUserRepositoryの実装クラス
 * Spring Data JPAを使用してデータアクセスを行う
 */
@Component
public class UserRepositoryImpl implements IUserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryImpl(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity = UserJpaEntity.fromDomainEntity(user);
        UserJpaEntity savedEntity = springDataUserRepository.save(jpaEntity);
        return savedEntity.toDomainEntity();
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return springDataUserRepository.findById(userId.value())
            .map(UserJpaEntity::toDomainEntity);
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return springDataUserRepository.findByUsername(username.value())
            .map(UserJpaEntity::toDomainEntity);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataUserRepository.findByEmail(email.value())
            .map(UserJpaEntity::toDomainEntity);
    }

    @Override
    public void delete(UserId userId) {
        springDataUserRepository.deleteById(userId.value());
    }
}
