package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.exception.EntityNotFoundException;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * DeleteTweetUseCase
 * ツイート削除のユースケース
 *
 * 責務:
 * - 投稿者本人のみ削除可能な権限チェック
 * - is_deletedフラグをtrueに設定(論理削除)
 * - トランザクション境界を管理
 */
@Service
@Transactional
public class DeleteTweetUseCase {

    private final ITweetRepository tweetRepository;

    public DeleteTweetUseCase(ITweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    /**
     * ツイート削除を実行
     * @param tweetId 削除するツイートのID
     * @param userId 削除リクエストを送信したユーザーID
     * @throws EntityNotFoundException ツイートが存在しない場合
     * @throws SecurityException 投稿者以外が削除しようとした場合
     * @throws IllegalStateException 既に削除済みの場合
     * @throws NullPointerException tweetIdまたはuserIdがnullの場合
     */
    public void execute(TweetId tweetId, UserId userId) {
        // 1. バリデーション
        if (tweetId == null) {
            throw new NullPointerException("TweetId cannot be null");
        }
        if (userId == null) {
            throw new NullPointerException("UserId cannot be null");
        }

        // 2. ツイートを検索
        Optional<Tweet> tweetOptional = tweetRepository.findById(tweetId);
        if (tweetOptional.isEmpty()) {
            throw new EntityNotFoundException("ツイートが見つかりません: " + tweetId.value());
        }

        Tweet tweet = tweetOptional.get();

        // 3. ツイートを論理削除（権限チェックを含む）
        tweet.delete(userId);

        // 4. データベースに永続化
        tweetRepository.save(tweet);
    }
}
