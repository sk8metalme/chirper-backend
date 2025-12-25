package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.service.TimelineService;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetTimelineUseCase {

    private final IFollowRepository followRepository;
    private final TimelineService timelineService;
    private final ILikeRepository likeRepository;
    private final IRetweetRepository retweetRepository;
    private final IUserRepository userRepository;

    public GetTimelineUseCase(
        IFollowRepository followRepository,
        TimelineService timelineService,
        ILikeRepository likeRepository,
        IRetweetRepository retweetRepository,
        IUserRepository userRepository
    ) {
        this.followRepository = followRepository;
        this.timelineService = timelineService;
        this.likeRepository = likeRepository;
        this.retweetRepository = retweetRepository;
        this.userRepository = userRepository;
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

        if (tweets.isEmpty()) {
            return new TimelineResult(List.of());
        }

        // ツイートIDリストを取得
        List<TweetId> tweetIds = tweets.stream()
            .map(Tweet::getId)
            .collect(Collectors.toList());

        // ユーザーIDリストを取得
        List<UserId> authorIds = tweets.stream()
            .map(Tweet::getUserId)
            .distinct()
            .collect(Collectors.toList());

        // バッチ取得（N+1クエリ回避）
        Map<UserId, User> authorMap = userRepository.findByIds(authorIds);
        Map<TweetId, Long> likesCountMap = likeRepository.countByTweetIds(tweetIds);
        Map<TweetId, Long> retweetsCountMap = retweetRepository.countByTweetIds(tweetIds);

        // 現在のユーザーがいいね/リツイートしたツイートをバッチ取得
        List<TweetId> likedTweetIds = likeRepository.findTweetIdsByUserId(currentUserId);
        List<TweetId> retweetedTweetIds = retweetRepository.findTweetIdsByUserId(currentUserId);

        // 各ツイートの詳細情報を構築
        List<TweetWithDetails> tweetsWithDetails = tweets.stream()
            .map(tweet -> {
                boolean liked = likedTweetIds.contains(tweet.getId());
                boolean retweeted = retweetedTweetIds.contains(tweet.getId());
                User author = authorMap.get(tweet.getUserId());
                long likesCount = likesCountMap.getOrDefault(tweet.getId(), 0L);
                long retweetsCount = retweetsCountMap.getOrDefault(tweet.getId(), 0L);
                return new TweetWithDetails(tweet, author, likesCount, retweetsCount, liked, retweeted);
            })
            .collect(Collectors.toList());

        return new TimelineResult(tweetsWithDetails);
    }

    public record TimelineResult(List<TweetWithDetails> tweets) {}

    public record TweetWithDetails(
        Tweet tweet,
        User author,
        long likesCount,
        long retweetsCount,
        boolean likedByCurrentUser,
        boolean retweetedByCurrentUser
    ) {}
}
