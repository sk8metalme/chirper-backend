package com.chirper.infrastructure.persistence.repository;

import com.chirper.domain.entity.Follow;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.valueobject.UserId;
import com.chirper.infrastructure.persistence.entity.FollowJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * FollowRepositoryImpl
 * IFollowRepositoryの実装クラス
 * Spring Data JPAを使用してデータアクセスを行う
 */
@Component
public class FollowRepositoryImpl implements IFollowRepository {

    private final SpringDataFollowRepository springDataFollowRepository;

    public FollowRepositoryImpl(SpringDataFollowRepository springDataFollowRepository) {
        this.springDataFollowRepository = springDataFollowRepository;
    }

    @Override
    public Follow save(Follow follow) {
        FollowJpaEntity jpaEntity = FollowJpaEntity.fromDomainEntity(follow);
        FollowJpaEntity savedEntity = springDataFollowRepository.save(jpaEntity);
        return savedEntity.toDomainEntity();
    }

    @Override
    public List<UserId> findFollowedUserIds(UserId followerUserId) {
        List<UUID> uuidList = springDataFollowRepository
            .findFollowedUserIdsByFollowerUserId(followerUserId.value());

        return uuidList.stream()
            .map(UserId::new)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Follow> findByFollowerAndFollowed(UserId followerUserId, UserId followedUserId) {
        return springDataFollowRepository
            .findByFollowerUserIdAndFollowedUserId(followerUserId.value(), followedUserId.value())
            .map(FollowJpaEntity::toDomainEntity);
    }

    @Override
    @Transactional
    public void delete(UserId followerUserId, UserId followedUserId) {
        springDataFollowRepository
            .deleteByFollowerUserIdAndFollowedUserId(followerUserId.value(), followedUserId.value());
    }
}
