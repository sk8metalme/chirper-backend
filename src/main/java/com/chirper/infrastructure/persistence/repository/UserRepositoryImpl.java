package com.chirper.infrastructure.persistence.repository;

import com.chirper.domain.entity.User;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import com.chirper.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public List<User> searchByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return springDataUserRepository.searchByKeyword(keyword, pageable)
            .stream()
            .map(UserJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

    @Override
    public long countByKeyword(String keyword) {
        return springDataUserRepository.countByKeyword(keyword);
    }

    @Override
    public Map<UserId, User> findByIds(List<UserId> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        List<java.util.UUID> uuidList = userIds.stream()
            .map(UserId::value)
            .collect(Collectors.toList());

        List<UserJpaEntity> entities = springDataUserRepository.findAllById(uuidList);

        return entities.stream()
            .collect(Collectors.toMap(
                entity -> new UserId(entity.getId()),
                UserJpaEntity::toDomainEntity
            ));
    }
}
