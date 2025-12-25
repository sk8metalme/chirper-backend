package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.exception.EntityNotFoundException;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.TweetId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * GetTweetUseCase
 * ツイート取得ユースケース
 *
 * 責務:
 * - 指定されたツイートIDのツイートを取得
 * - いいね数とリツイート数を集計
 */
@Service
@Transactional(readOnly = true)
public class GetTweetUseCase {

    private final ITweetRepository tweetRepository;
    private final ILikeRepository likeRepository;
    private final IRetweetRepository retweetRepository;

    public GetTweetUseCase(
        ITweetRepository tweetRepository,
        ILikeRepository likeRepository,
        IRetweetRepository retweetRepository
    ) {
        this.tweetRepository = tweetRepository;
        this.likeRepository = likeRepository;
        this.retweetRepository = retweetRepository;
    }

    /**
     * ツイート取得を実行
     *
     * @param tweetId ツイートID
     * @return ツイート情報、いいね数、リツイート数
     * @throws NullPointerException tweetIdがnullの場合
     * @throws EntityNotFoundException ツイートが見つからない場合
     */
    public TweetResult execute(TweetId tweetId) {
        if (tweetId == null) {
            throw new NullPointerException("TweetId cannot be null");
        }

        // ツイートを取得
        Tweet tweet = tweetRepository.findById(tweetId)
            .orElseThrow(() -> new EntityNotFoundException("ツイートが見つかりません"));

        // いいね数とリツイート数を取得
        int likesCount = likeRepository.findByTweetId(tweetId).size();
        int retweetsCount = retweetRepository.findByTweetId(tweetId).size();

        return new TweetResult(tweet, likesCount, retweetsCount);
    }

    /**
     * ツイート取得結果
     *
     * @param tweet ツイートエンティティ
     * @param likesCount いいね数
     * @param retweetsCount リツイート数
     */
    public record TweetResult(Tweet tweet, int likesCount, int retweetsCount) {}
}
