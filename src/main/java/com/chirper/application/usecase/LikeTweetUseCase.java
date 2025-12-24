package com.chirper.application.usecase;

import com.chirper.domain.entity.Like;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class LikeTweetUseCase {

    private final ILikeRepository likeRepository;

    public LikeTweetUseCase(ILikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public void execute(UserId userId, TweetId tweetId) {
        if (userId == null) {
            throw new NullPointerException("UserId cannot be null");
        }
        if (tweetId == null) {
            throw new NullPointerException("TweetId cannot be null");
        }

        Optional<Like> existingLike = likeRepository.findByUserIdAndTweetId(userId, tweetId);
        if (existingLike.isPresent()) {
            throw new IllegalStateException("Already liked this tweet");
        }

        Like like = Like.create(userId, tweetId);
        likeRepository.save(like);
    }
}
