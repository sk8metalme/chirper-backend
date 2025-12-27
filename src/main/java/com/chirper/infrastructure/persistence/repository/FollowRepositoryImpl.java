package com.chirper.infrastructure.persistence.repository;

import com.chirper.domain.entity.Follow;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.valueobject.UserId;
import com.chirper.infrastructure.persistence.entity.FollowJpaEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
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
    @Transactional
    public Follow save(Follow follow) {
        try {
            FollowJpaEntity jpaEntity = FollowJpaEntity.fromDomainEntity(follow);
            FollowJpaEntity savedEntity = springDataFollowRepository.save(jpaEntity);
            return savedEntity.toDomainEntity();
        } catch (DataIntegrityViolationException e) {
            // 並行実行時の重複挿入を検知した場合、明確なエラーメッセージをスロー
            throw new IllegalStateException(
                "Follow relationship already exists between follower and followed user", e);
        }
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

    @Override
    public long countFollowers(UserId userId) {
        return springDataFollowRepository.countByFollowedUserId(userId.value());
    }

    @Override
    public long countFollowing(UserId userId) {
        return springDataFollowRepository.countByFollowerUserId(userId.value());
    }

    @Override
    public boolean existsByFollowerAndFollowed(UserId followerUserId, UserId followedUserId) {
        return springDataFollowRepository
            .existsByFollowerUserIdAndFollowedUserId(followerUserId.value(), followedUserId.value());
    }

    @Override
    public List<UserId> findFollowerUserIds(UserId followedUserId, int offset, int limit) {
        // offsetがlimitで割り切れない場合は呼び出し元のロジックに問題がある
        if (offset % limit != 0) {
            throw new IllegalArgumentException("offset must be a multiple of limit");
        }
        int pageNumber = offset / limit;
        PageRequest pageRequest = PageRequest.of(pageNumber, limit);
        List<UUID> uuidList = springDataFollowRepository.findFollowerUserIds(
            followedUserId.value(),
            pageRequest
        );
        return uuidList.stream()
            .map(UserId::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserId> findFollowingUserIds(UserId followerUserId, int offset, int limit) {
        // offsetがlimitで割り切れない場合は呼び出し元のロジックに問題がある
        if (offset % limit != 0) {
            throw new IllegalArgumentException("offset must be a multiple of limit");
        }
        int pageNumber = offset / limit;
        PageRequest pageRequest = PageRequest.of(pageNumber, limit);
        List<UUID> uuidList = springDataFollowRepository.findFollowingUserIds(
            followerUserId.value(),
            pageRequest
        );
        return uuidList.stream()
            .map(UserId::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserId> findFollowedUserIdsIn(UserId followerUserId, List<UserId> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return List.of();
        }

        List<UUID> targetUuids = targetUserIds.stream()
            .map(UserId::value)
            .collect(Collectors.toList());

        List<UUID> followedUuids = springDataFollowRepository.findFollowedUserIdsIn(
            followerUserId.value(),
            targetUuids
        );

        return followedUuids.stream()
            .map(UserId::new)
            .collect(Collectors.toList());
    }
}
