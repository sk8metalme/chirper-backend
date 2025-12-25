package com.chirper.application.usecase;

import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UnlikeTweetUseCase
 * ツイートいいね解除ユースケース
 *
 * 責務:
 * - いいね記録を削除
 */
@Service
@Transactional
public class UnlikeTweetUseCase {

    private final ILikeRepository likeRepository;

    public UnlikeTweetUseCase(ILikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    /**
     * いいね解除を実行
     *
     * @param userId いいねを解除するユーザーID
     * @param tweetId いいね解除対象ツイートID
     * @throws NullPointerException userIdまたはtweetIdがnullの場合
     */
    public void execute(UserId userId, TweetId tweetId) {
        if (userId == null) {
            throw new NullPointerException("UserId cannot be null");
        }
        if (tweetId == null) {
            throw new NullPointerException("TweetId cannot be null");
        }

        likeRepository.delete(userId, tweetId);
    }
}
