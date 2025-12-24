package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.service.TimelineService;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetTimelineUseCase {

    private final IFollowRepository followRepository;
    private final TimelineService timelineService;
    private final ILikeRepository likeRepository;
    private final IRetweetRepository retweetRepository;

    public GetTimelineUseCase(IFollowRepository followRepository,
                               TimelineService timelineService,
                               ILikeRepository likeRepository,
                               IRetweetRepository retweetRepository) {
        this.followRepository = followRepository;
        this.timelineService = timelineService;
        this.likeRepository = likeRepository;
        this.retweetRepository = retweetRepository;
    }

    public TimelineResult execute(UserId currentUserId, int page, int size) {
        if (currentUserId == null) {
            throw new NullPointerException("UserId cannot be null");
        }

        List<UserId> followedUserIds = followRepository.findFollowedUserIds(currentUserId);

        if (followedUserIds.isEmpty()) {
            return new TimelineResult(List.of());
        }

        List<Tweet> tweets = timelineService.getTimeline(followedUserIds, page, size);

        List<TweetWithStatus> tweetsWithStatus = tweets.stream()
            .map(tweet -> {
                boolean liked = likeRepository.findByUserIdAndTweetId(currentUserId, tweet.getId()).isPresent();
                boolean retweeted = retweetRepository.findByUserIdAndTweetId(currentUserId, tweet.getId()).isPresent();
                return new TweetWithStatus(tweet, liked, retweeted);
            })
            .collect(Collectors.toList());

        return new TimelineResult(tweetsWithStatus);
    }

    public record TimelineResult(List<TweetWithStatus> tweets) {}
    public record TweetWithStatus(Tweet tweet, boolean likedByCurrentUser, boolean retweetedByCurrentUser) {}
}
