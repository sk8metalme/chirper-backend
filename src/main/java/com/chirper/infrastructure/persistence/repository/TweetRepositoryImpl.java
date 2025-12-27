package com.chirper.infrastructure.persistence.repository;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import com.chirper.infrastructure.persistence.entity.TweetJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TweetRepositoryImpl
 * ITweetRepositoryの実装クラス
 * Spring Data JPAを使用してデータアクセスを行う
 * N+1クエリ問題を回避するための最適化を実装
 */
@Component
public class TweetRepositoryImpl implements ITweetRepository {

    private final SpringDataTweetRepository springDataTweetRepository;

    public TweetRepositoryImpl(SpringDataTweetRepository springDataTweetRepository) {
        this.springDataTweetRepository = springDataTweetRepository;
    }

    @Override
    public Tweet save(Tweet tweet) {
        TweetJpaEntity jpaEntity = TweetJpaEntity.fromDomainEntity(tweet);
        TweetJpaEntity savedEntity = springDataTweetRepository.save(jpaEntity);
        return savedEntity.toDomainEntity();
    }

    @Override
    public Optional<Tweet> findById(TweetId tweetId) {
        return springDataTweetRepository.findByIdAndIsDeletedFalse(tweetId.value())
            .map(TweetJpaEntity::toDomainEntity);
    }

    @Override
    public List<Tweet> findByUserIdsWithDetails(List<UserId> userIds, int page, int size) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        List<UUID> uuidList = userIds.stream()
            .map(UserId::value)
            .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return springDataTweetRepository.findByUserIdInAndIsDeletedFalse(uuidList, pageable)
            .stream()
            .map(TweetJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(TweetId tweetId) {
        // This method cannot enforce domain rules (authorization, already-deleted checks)
        // because it lacks the UserId parameter needed for tweet.delete(userId).
        // Deletion must be performed at the Application layer using this pattern:
        // 1. Optional<Tweet> tweet = repository.findById(tweetId)
        // 2. tweet.delete(userId) - enforces domain rules
        // 3. repository.save(tweet) - persists isDeleted flag
        throw new UnsupportedOperationException(
            "Physical delete is not supported. Use Application layer to perform logical delete: " +
            "load tweet -> tweet.delete(userId) -> save(tweet)"
        );
    }

    @Override
    public List<Tweet> searchByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return springDataTweetRepository.searchByKeyword(keyword, pageable)
            .stream()
            .map(TweetJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

    @Override
    public long countByKeyword(String keyword) {
        return springDataTweetRepository.countByKeyword(keyword);
    }

    @Override
    public List<Tweet> findByUserId(UserId userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return springDataTweetRepository.findByUserIdAndIsDeletedFalse(userId.value(), pageable)
            .stream()
            .map(TweetJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }
}
