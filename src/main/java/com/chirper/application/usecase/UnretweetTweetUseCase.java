package com.chirper.application.usecase;

import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UnretweetTweetUseCase
 * リツイート解除ユースケース
 *
 * 責務:
 * - リツイート記録を削除
 */
@Service
@Transactional
public class UnretweetTweetUseCase {

    private final IRetweetRepository retweetRepository;

    public UnretweetTweetUseCase(IRetweetRepository retweetRepository) {
        this.retweetRepository = retweetRepository;
    }

    /**
     * リツイート解除を実行
     *
     * @param userId リツイートを解除するユーザーID
     * @param tweetId リツイート解除対象ツイートID
     * @throws NullPointerException userIdまたはtweetIdがnullの場合
     */
    public void execute(UserId userId, TweetId tweetId) {
        if (userId == null) {
            throw new NullPointerException("UserId cannot be null");
        }
        if (tweetId == null) {
            throw new NullPointerException("TweetId cannot be null");
        }

        retweetRepository.delete(userId, tweetId);
    }
}
