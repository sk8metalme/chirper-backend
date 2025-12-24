package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CreateTweetUseCase
 * ツイート投稿のユースケース
 *
 * 責務:
 * - Tweet Entity生成(TweetContentのバリデーションを含む)
 * - ITweetRepository.save()でデータベースに永続化
 * - トランザクション境界を管理
 */
@Service
@Transactional
public class CreateTweetUseCase {

    private final ITweetRepository tweetRepository;

    public CreateTweetUseCase(ITweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    /**
     * ツイート投稿を実行
     * @param userId 投稿者のユーザーID
     * @param contentString ツイート本文
     * @return 投稿されたTweet Entity
     * @throws IllegalArgumentException ツイート本文が不正な場合
     * @throws NullPointerException userIdがnullの場合
     */
    public Tweet execute(UserId userId, String contentString) {
        // 1. バリデーション
        if (userId == null) {
            throw new NullPointerException("UserId cannot be null");
        }

        // 2. Value Objectsを生成（バリデーション）
        TweetContent content = new TweetContent(contentString);

        // 3. Tweet Entityを生成
        Tweet tweet = Tweet.create(userId, content);

        // 4. データベースに永続化
        return tweetRepository.save(tweet);
    }
}
