package com.chirper.infrastructure.persistence.repository;

import com.chirper.domain.entity.Like;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import com.chirper.infrastructure.persistence.entity.LikeJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
}
