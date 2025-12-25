package com.chirper.infrastructure.persistence.repository;

import com.chirper.domain.entity.Like;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import com.chirper.infrastructure.persistence.entity.LikeJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * LikeRepositoryImpl
 * ILikeRepositoryの実装クラス
 * Spring Data JPAを使用してデータアクセスを行う
 */
@Component
public class LikeRepositoryImpl implements ILikeRepository {

    private final SpringDataLikeRepository springDataLikeRepository;

    public LikeRepositoryImpl(SpringDataLikeRepository springDataLikeRepository) {
        this.springDataLikeRepository = springDataLikeRepository;
    }

    @Override
    public Like save(Like like) {
        LikeJpaEntity jpaEntity = LikeJpaEntity.fromDomainEntity(like);
        LikeJpaEntity savedEntity = springDataLikeRepository.save(jpaEntity);
        return savedEntity.toDomainEntity();
    }

    @Override
    public List<Like> findByTweetId(TweetId tweetId) {
        return springDataLikeRepository.findByTweetId(tweetId.value())
            .stream()
            .map(LikeJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<Like> findByUserId(UserId userId) {
        return springDataLikeRepository.findByUserId(userId.value())
            .stream()
            .map(LikeJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Like> findByUserIdAndTweetId(UserId userId, TweetId tweetId) {
        return springDataLikeRepository.findByUserIdAndTweetId(userId.value(), tweetId.value())
            .map(LikeJpaEntity::toDomainEntity);
    }

    @Override
    @Transactional
    public void delete(UserId userId, TweetId tweetId) {
        springDataLikeRepository.deleteByUserIdAndTweetId(userId.value(), tweetId.value());
    }

    @Override
    public long countByTweetId(TweetId tweetId) {
        return springDataLikeRepository.countByTweetId(tweetId.value());
    }

    @Override
    public Map<TweetId, Long> countByTweetIds(List<TweetId> tweetIds) {
        if (tweetIds == null || tweetIds.isEmpty()) {
            return new HashMap<>();
        }

        List<UUID> uuidList = tweetIds.stream()
            .map(TweetId::value)
            .collect(Collectors.toList());

        List<Object[]> results = springDataLikeRepository.countByTweetIds(uuidList);

        return results.stream()
            .collect(Collectors.toMap(
                row -> new TweetId((UUID) row[0]),
                row -> (Long) row[1]
            ));
    }
}
