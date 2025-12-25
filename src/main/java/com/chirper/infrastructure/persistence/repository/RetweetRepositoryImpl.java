package com.chirper.infrastructure.persistence.repository;

import com.chirper.domain.entity.Retweet;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import com.chirper.infrastructure.persistence.entity.RetweetJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RetweetRepositoryImpl
 * IRetweetRepositoryの実装クラス
 * Spring Data JPAを使用してデータアクセスを行う
 */
@Component
public class RetweetRepositoryImpl implements IRetweetRepository {

    private final SpringDataRetweetRepository springDataRetweetRepository;

    public RetweetRepositoryImpl(SpringDataRetweetRepository springDataRetweetRepository) {
        this.springDataRetweetRepository = springDataRetweetRepository;
    }

    @Override
    public Retweet save(Retweet retweet) {
        RetweetJpaEntity jpaEntity = RetweetJpaEntity.fromDomainEntity(retweet);
        RetweetJpaEntity savedEntity = springDataRetweetRepository.save(jpaEntity);
        return savedEntity.toDomainEntity();
    }

    @Override
    public List<Retweet> findByTweetId(TweetId tweetId) {
        return springDataRetweetRepository.findByTweetId(tweetId.value())
            .stream()
            .map(RetweetJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<Retweet> findByUserId(UserId userId) {
        return springDataRetweetRepository.findByUserId(userId.value())
            .stream()
            .map(RetweetJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Retweet> findByUserIdAndTweetId(UserId userId, TweetId tweetId) {
        return springDataRetweetRepository.findByUserIdAndTweetId(userId.value(), tweetId.value())
            .map(RetweetJpaEntity::toDomainEntity);
    }

    @Override
    @Transactional
    public void delete(UserId userId, TweetId tweetId) {
        springDataRetweetRepository.deleteByUserIdAndTweetId(userId.value(), tweetId.value());
    }

    @Override
    public long countByTweetId(TweetId tweetId) {
        return springDataRetweetRepository.countByTweetId(tweetId.value());
    }

    @Override
    public Map<TweetId, Long> countByTweetIds(List<TweetId> tweetIds) {
        if (tweetIds == null || tweetIds.isEmpty()) {
            return new HashMap<>();
        }

        List<UUID> uuidList = tweetIds.stream()
            .map(TweetId::value)
            .collect(Collectors.toList());

        List<Object[]> results = springDataRetweetRepository.countByTweetIds(uuidList);

        return results.stream()
            .collect(Collectors.toMap(
                row -> new TweetId((UUID) row[0]),
                row -> (Long) row[1]
            ));
    }

    @Override
    public List<TweetId> findTweetIdsByUserId(UserId userId) {
        return springDataRetweetRepository.findTweetIdsByUserId(userId.value())
            .stream()
            .map(TweetId::new)
            .collect(Collectors.toList());
    }
}
