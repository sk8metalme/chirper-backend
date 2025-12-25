package com.chirper.application.usecase;

import com.chirper.domain.entity.Retweet;
import com.chirper.domain.exception.DuplicateEntityException;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class RetweetUseCase {

    private final IRetweetRepository retweetRepository;

    public RetweetUseCase(IRetweetRepository retweetRepository) {
        this.retweetRepository = retweetRepository;
    }

    public void execute(UserId userId, TweetId tweetId) {
        if (userId == null) {
            throw new NullPointerException("UserId cannot be null");
        }
        if (tweetId == null) {
            throw new NullPointerException("TweetId cannot be null");
        }

        Optional<Retweet> existingRetweet = retweetRepository.findByUserIdAndTweetId(userId, tweetId);
        if (existingRetweet.isPresent()) {
            throw new DuplicateEntityException("既にこのツイートをリツイートしています");
        }

        Retweet retweet = Retweet.create(userId, tweetId);
        retweetRepository.save(retweet);
    }
}
