# 実装タスク

## タスク概要

chirper-backendの実装タスクは、オニオンアーキテクチャの依存関係に従い、内側の層(Domain)から外側の層(Presentation)へと段階的に構築します。すべての要件を網羅し、テストカバレッジ95%以上を達成します。

---

## タスクリスト

### Phase 1: プロジェクトセットアップとインフラ基盤

- [ ] 1. プロジェクト初期化とビルド環境構築
- [x] 1.1 (P) Gradleプロジェクト初期化とSpring Boot設定
  - Spring Boot 3.4.13、Java 21 LTSでGradleプロジェクトを初期化
  - build.gradleに必要な依存関係を追加(Spring Data JPA、Spring Security、jjwt、Flyway、TestContainers等)
  - application.ymlでデータベース接続、HikariCP設定(最小10、最大50)、ログ設定を構成
  - _Requirements: 7.1, 8.1, 8.2, 9.3_

- [x] 1.2 (P) PostgreSQLデータベース環境セットアップ
  - ローカル開発環境でPostgreSQL 17.xを起動(Docker Compose推奨)
  - Flywayマイグレーション設定を構成(src/main/resources/db/migration/)
  - 接続テストを実行し、データベースアクセスを確認
  - _Requirements: 8.1, 8.2_

- [ ] 2. データベーススキーマ設計とマイグレーション
- [x] 2.1 usersテーブル作成マイグレーション
  - V1__create_users_table.sql でusersテーブルを作成(id: UUID、username: VARCHAR(20) UNIQUE、email: VARCHAR(255) UNIQUE、password_hash、display_name、bio、avatar_url、created_at、updated_at)
  - usernameとemailにUNIQUE制約とインデックスを設定
  - UTF-8文字エンコーディング、UTC日時を設定
  - _Requirements: 8.3, 8.4, 8.5, 8.7, 8.9, 8.10_

- [x] 2.2 tweetsテーブル作成マイグレーション
  - V2__create_tweets_table.sql でtweetsテーブルを作成(id: UUID、user_id: UUID FK、content: TEXT、created_at、updated_at、is_deleted: BOOLEAN)
  - user_idにFOREIGN KEY制約(ON DELETE CASCADE)を設定
  - content長制限(280文字)をCHECK制約で強制
  - user_id + created_at DESCのインデックスを作成
  - _Requirements: 8.3, 8.4, 8.6, 8.7, 8.8_

- [x] 2.3 (P) followsテーブル作成マイグレーション
  - V3__create_follows_table.sql でfollowsテーブルを作成(id: UUID、follower_user_id: UUID FK、followed_user_id: UUID FK、created_at)
  - follower_user_id + followed_user_idに複合UNIQUE制約を設定
  - 自己フォロー防止のCHECK制約を追加
  - follower_user_id、followed_user_idにインデックスを作成
  - _Requirements: 8.3, 8.4, 8.6, 8.7, 8.8_

- [x] 2.4 (P) likesテーブル作成マイグレーション
  - V4__create_likes_table.sql でlikesテーブルを作成(id: UUID、user_id: UUID FK、tweet_id: UUID FK、created_at)
  - user_id + tweet_idに複合UNIQUE制約を設定
  - user_id、tweet_idにインデックスを作成
  - _Requirements: 8.3, 8.4, 8.6, 8.7, 8.8_

- [x] 2.5 (P) retweetsテーブル作成マイグレーション
  - V5__create_retweets_table.sql でretweetsテーブルを作成(id: UUID、user_id: UUID FK、tweet_id: UUID FK、created_at)
  - user_id + tweet_idに複合UNIQUE制約を設定
  - user_id、tweet_idにインデックスを作成
  - _Requirements: 8.3, 8.4, 8.6, 8.7, 8.8_

### Phase 2: Domain層実装(ビジネスロジック)

- [ ] 3. Value Objects実装
- [x] 3.1 (P) ユーザー関連Value Objects実装
  - UserId、Username、Email、Password recordを実装
  - Usernameは3-20文字バリデーション、Emailはメール形式バリデーション
  - Password.fromPlainText()でbcryptハッシュ化(コスト係数10)、matches()でパスワード検証
  - すべてのValue Objectのコンストラクタでビジネスルール検証を強制
  - _Requirements: 1.1, 1.2, 1.5, 7.2_

- [x] 3.2 (P) ツイート関連Value Objects実装
  - TweetId、TweetContent recordを実装
  - TweetContentは1-280文字バリデーション
  - 不変性を保証(recordの特性を活用)
  - _Requirements: 2.1, 2.2, 7.2_

- [ ] 4. Domain Entities実装
- [x] 4.1 User Entity実装
  - User EntityをUserId、Username、Email、Password、プロフィール情報で構成
  - パスワードハッシュ化ロジック、プロフィール更新メソッドを実装
  - ユーザー名・メールアドレスの一意性をビジネスルールとして定義
  - _Requirements: 1.1, 1.2, 1.7, 7.2_

- [x] 4.2 (P) Tweet Entity実装
  - Tweet EntityをTweetId、UserId、TweetContent、is_deleted、createdAtで構成
  - 論理削除フラグ管理、投稿者による削除権限検証メソッドを実装
  - TweetContentのバリデーションをValue Objectで強制
  - _Requirements: 2.1, 2.2, 2.4, 2.5, 2.6, 7.2_

- [x] 4.3 (P) Follow Entity実装
  - Follow EntityをFollowId、followerUserId、followedUserId、createdAtで構成
  - 自分自身をフォローできないビジネスルールを検証
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 7.2_

- [x] 4.4 (P) Like Entity実装
  - Like EntityをLikeId、userId、tweetId、createdAtで構成
  - _Requirements: 4.5, 4.6, 4.7, 7.2_

- [x] 4.5 (P) Retweet Entity実装
  - Retweet EntityをRetweetId、userId、tweetId、createdAtで構成
  - _Requirements: 4.8, 4.9, 4.10, 7.2_

- [ ] 5. Repository Interfaces定義
- [x] 5.1 (P) IUserRepository Interface定義
  - save()、findById()、findByUsername()、findByEmail()、delete()メソッドを定義
  - Domain層で定義し、Infrastructure層での実装を想定(依存性逆転の原則)
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.7, 7.7_

- [x] 5.2 (P) ITweetRepository Interface定義
  - save()、findById()、findByUserIdsWithDetails()、delete()メソッドを定義
  - タイムライン取得用のfindByUserIdsWithDetails()でN+1クエリ対策を考慮
  - _Requirements: 2.1, 2.3, 2.4, 3.1, 3.5, 7.7_

- [x] 5.3 (P) IFollowRepository Interface定義
  - save()、findFollowedUserIds()、delete()メソッドを定義
  - _Requirements: 3.1, 4.1, 4.2, 4.3, 7.7_

- [x] 5.4 (P) ILikeRepository Interface定義
  - save()、findByTweetId()、findByUserId()、delete()メソッドを定義
  - _Requirements: 4.5, 4.6, 4.7, 7.7_

- [x] 5.5 (P) IRetweetRepository Interface定義
  - save()、findByTweetId()、findByUserId()、delete()メソッドを定義
  - _Requirements: 4.8, 4.9, 4.10, 7.7_

- [ ] 6. Domain Services実装
- [x] 6.1 AuthenticationService実装
  - authenticate()メソッドでパスワード検証(bcrypt)
  - generateJwtToken()メソッドでJWT生成(有効期限1時間、HS256署名)
  - validateJwtToken()メソッドでJWT検証(署名、有効期限チェック)
  - JWTシークレットキーは環境変数で管理
  - _Requirements: 1.3, 1.4, 1.5, 1.6, 6.1, 6.2, 6.3_

- [x] 6.2 (P) TimelineService実装
  - getTimeline()メソッドでフォローユーザーのツイートを時系列取得
  - N+1クエリ問題を回避する設計(ITweetRepositoryのfindByUserIdsWithDetails()を使用)
  - ページネーション処理を実装
  - _Requirements: 3.1, 3.2, 3.5_

- [x] 6.3 (P) FollowService実装
  - フォロー関係の妥当性検証(自分自身をフォローしない等)
  - フォロー済みチェック、フォロー解除ロジック
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

### Phase 3: Infrastructure層実装(データアクセス)

- [ ] 7. JPA Entities実装
- [x] 7.1 (P) UserJpaEntity実装
  - @Entity、@Table、@Idアノテーションでusersテーブルにマッピング
  - Domain EntityとJPA Entityの変換メソッド(toJpaEntity()、toDomainEntity())を実装
  - _Requirements: 7.4, 8.3_

- [x] 7.2 (P) TweetJpaEntity実装
  - @Entity、@Table、@Idアノテーションでtweetsテーブルにマッピング
  - Domain EntityとJPA Entityの変換メソッドを実装
  - _Requirements: 7.4, 8.3_

- [x] 7.3 (P) FollowJpaEntity実装
  - @Entity、@Table、@Idアノテーションでfollowsテーブルにマッピング
  - Domain EntityとJPA Entityの変換メソッドを実装
  - _Requirements: 7.4, 8.3_

- [x] 7.4 (P) LikeJpaEntity実装
  - @Entity、@Table、@Idアノテーションでlikesテーブルにマッピング
  - Domain EntityとJPA Entityの変換メソッドを実装
  - _Requirements: 7.4, 8.3_

- [x] 7.5 (P) RetweetJpaEntity実装
  - @Entity、@Table、@Idアノテーションでretweetsテーブルにマッピング
  - Domain EntityとJPA Entityの変換メソッドを実装
  - _Requirements: 7.4, 8.3_

- [ ] 8. Repository Implementations実装
- [x] 8.1 (P) UserRepositoryImpl実装
  - IUserRepositoryを実装し、Spring Data JPA Repositoryを使用
  - findByUsername()、findByEmail()でユーザー検索
  - Domain EntityとJPA Entityの変換を行う
  - _Requirements: 7.4, 7.7_

- [x] 8.2 TweetRepositoryImpl実装(N+1クエリ対策含む)
  - ITweetRepositoryを実装し、JOIN FETCHまたは@EntityGraphでN+1クエリ対策
  - findByUserIdsWithDetails()でタイムライン取得時のイーガーロードを実装
  - 論理削除されたツイート(is_deleted=true)を除外
  - _Requirements: 3.5, 7.4, 7.7, 9.4_

- [x] 8.3 (P) FollowRepositoryImpl実装
  - IFollowRepositoryを実装し、フォローユーザーID取得ロジックを実装
  - _Requirements: 7.4, 7.7_

- [x] 8.4 (P) LikeRepositoryImpl実装
  - ILikeRepositoryを実装し、いいね済み状態判定ロジックを実装
  - _Requirements: 7.4, 7.7_

- [x] 8.5 (P) RetweetRepositoryImpl実装
  - IRetweetRepositoryを実装し、リツイート済み状態判定ロジックを実装
  - _Requirements: 7.4, 7.7_

### Phase 4: Application層実装(ユースケース)

- [ ] 9. 認証・ユーザー管理Use Cases実装
- [ ] 9.1 RegisterUserUseCase実装
  - ユーザー名重複チェック(IUserRepository.findByUsername())
  - User Entity生成(パスワードハッシュ化を含む)
  - IUserRepository.save()でデータベースに永続化
  - @Transactionalでトランザクション制御
  - _Requirements: 1.1, 1.2, 7.3_

- [ ] 9.2 LoginUserUseCase実装
  - IUserRepository.findByUsername()でユーザー取得
  - AuthenticationService.authenticate()でパスワード検証
  - 認証成功時、AuthenticationService.generateJwtToken()でJWT発行
  - @Transactionalでトランザクション制御
  - _Requirements: 1.3, 1.4, 7.3_

- [ ] 9.3 (P) UpdateProfileUseCase実装
  - プロフィール情報(displayName、bio、avatarUrl)を更新
  - 本人のみ更新可能な権限チェック
  - @Transactionalでトランザクション制御
  - _Requirements: 1.7, 7.3_

- [ ] 10. ツイート管理Use Cases実装
- [ ] 10.1 CreateTweetUseCase実装
  - Tweet Entity生成(TweetContentのバリデーションを含む)
  - ITweetRepository.save()でデータベースに永続化
  - @Transactionalでトランザクション制御
  - _Requirements: 2.1, 2.2, 7.3_

- [ ] 10.2 (P) DeleteTweetUseCase実装
  - 投稿者本人のみ削除可能な権限チェック
  - is_deletedフラグをtrueに設定(論理削除)
  - @Transactionalでトランザクション制御
  - _Requirements: 2.4, 2.5, 2.6, 7.3_

- [ ] 11. タイムライン機能Use Case実装
- [ ] 11.1 GetTimelineUseCase実装
  - IFollowRepository.findFollowedUserIds()でフォローユーザーID取得
  - TimelineService.getTimeline()でツイート取得(N+1クエリ対策)
  - ILikeRepository、IRetweetRepositoryで現在のユーザーのいいね・リツイート済み状態を判定
  - ページネーション処理(page、size)を実装
  - タイムラインレスポンスに総ページ数を含める
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 7.3_

- [ ] 12. ソーシャル機能Use Cases実装
- [ ] 12.1 (P) FollowUserUseCase実装
  - FollowService.validateFollow()でフォロー可能性を検証(自己フォロー禁止、重複フォロー禁止)
  - IFollowRepository.save()でフォロー関係を作成
  - @Transactionalでトランザクション制御
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 7.3_

- [ ] 12.2 (P) LikeTweetUseCase実装
  - いいね済みチェック(重複いいね禁止)
  - ILikeRepository.save()でいいね記録を作成
  - @Transactionalでトランザクション制御
  - _Requirements: 4.5, 4.6, 4.7, 7.3_

- [ ] 12.3 (P) RetweetUseCase実装
  - リツイート済みチェック(重複リツイート禁止)
  - IRetweetRepository.save()でリツイート記録を作成
  - @Transactionalでトランザクション制御
  - _Requirements: 4.8, 4.9, 4.10, 7.3_

- [ ] 13. 検索機能Use Case実装
- [ ] 13.1 SearchUseCase実装
  - キーワードが2文字以上であることを検証
  - IUserRepository、ITweetRepositoryで部分一致検索(username、displayName、content)
  - 論理削除されたツイート(is_deleted=true)を結果から除外
  - ページネーション処理を実装
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 7.3_

### Phase 5: Presentation層実装(REST API)

- [ ] 14. Request/Response DTOs実装
- [ ] 14.1 (P) 認証関連DTOs実装
  - RegisterRequest、RegisterResponse、LoginRequest、LoginResponseを実装
  - Bean Validation(@Valid、@Size、@Email、@NotNull)を適用
  - _Requirements: 11.1, 11.2, 11.15_

- [ ] 14.2 (P) ツイート関連DTOs実装
  - CreateTweetRequest、CreateTweetResponse、TweetResponse、TimelineResponseを実装
  - Bean Validation(@Size(max=280))でツイート本文を検証
  - _Requirements: 11.3, 11.4, 11.5, 11.6, 11.15_

- [ ] 14.3 (P) ユーザー関連DTOs実装
  - UserProfileResponse、UpdateProfileRequest、UpdateProfileResponseを実装
  - Bean Validationでプロフィール情報を検証
  - _Requirements: 11.7, 11.8, 11.15_

- [ ] 15. Controllers実装
- [ ] 15.1 AuthController実装
  - POST /api/v1/auth/register でユーザー登録を受け付け
  - POST /api/v1/auth/login でユーザー認証を受け付け
  - RegisterUserUseCase、LoginUserUseCaseを呼び出し
  - 適切なHTTPステータスコード(201 Created、200 OK、400 Bad Request、401 Unauthorized、409 Conflict)を返却
  - _Requirements: 11.1, 11.2, 11.19, 11.20_

- [ ] 15.2 (P) UserController実装
  - GET /api/v1/users/{username} でユーザープロフィール取得を提供
  - PUT /api/v1/users/profile でプロフィール更新を受け付け
  - JWT認証を必須とし、本人のみ更新可能
  - _Requirements: 11.7, 11.8, 11.18, 11.19, 11.20_

- [ ] 15.3 (P) TweetController実装
  - POST /api/v1/tweets でツイート投稿を受け付け
  - GET /api/v1/tweets/{tweetId} でツイート取得を提供
  - DELETE /api/v1/tweets/{tweetId} でツイート削除を受け付け
  - JWT認証を必須とし、投稿者のみ削除可能
  - _Requirements: 11.3, 11.4, 11.5, 11.18, 11.19, 11.20_

- [ ] 15.4 (P) TimelineController実装
  - GET /api/v1/timeline でタイムライン取得を提供
  - ページネーションパラメータ(page、size)を検証(デフォルト: page=0、size=20、最大100)
  - JWT認証を必須とし、ユーザーIDを抽出
  - _Requirements: 11.6, 11.18, 11.19, 11.20_

- [ ] 15.5 (P) SocialController実装
  - POST /api/v1/users/{userId}/follow でユーザーフォローを受け付け
  - DELETE /api/v1/users/{userId}/follow でユーザーフォロー解除を受け付け
  - POST /api/v1/tweets/{tweetId}/like でツイートいいねを受け付け
  - DELETE /api/v1/tweets/{tweetId}/like でツイートいいね解除を受け付け
  - POST /api/v1/tweets/{tweetId}/retweet でリツイートを受け付け
  - DELETE /api/v1/tweets/{tweetId}/retweet でリツイート解除を受け付け
  - JWT認証を必須とし、ユーザーIDを抽出
  - _Requirements: 11.9, 11.10, 11.11, 11.12, 11.13, 11.14, 11.18, 11.19, 11.20_

- [ ] 15.6 (P) SearchController実装
  - GET /api/v1/search でユーザー・ツイート検索を提供
  - クエリパラメータ(query、page、size)を検証(queryは2文字以上)
  - _Requirements: 5.1, 5.2, 11.19, 11.20_

- [ ] 16. セキュリティ設定実装
- [ ] 16.1 JwtAuthenticationFilter実装
  - Authorization: Bearer {token}ヘッダーからJWT取得
  - AuthenticationService.validateJwtToken()でトークン検証(署名、有効期限)
  - ユーザーIDを抽出し、SecurityContextに設定
  - トークン検証失敗時、401 Unauthorizedエラーを返却
  - _Requirements: 6.1, 6.2, 6.3, 11.18_

- [ ] 16.2 Spring Security設定実装
  - すべての保護されたエンドポイント(/api/v1/tweets、/api/v1/timeline等)でJWT認証を必須化
  - CORS設定でフロントエンドオリジンからのリクエストのみ許可
  - セキュリティヘッダー(Content-Security-Policy、X-Frame-Options、X-Content-Type-Options、Strict-Transport-Security)を設定
  - _Requirements: 6.1, 6.4, 6.7_

- [ ] 16.3 GlobalExceptionHandler実装
  - すべての例外をキャッチし、統一されたエラーレスポンス形式(error.code、error.message、error.details、error.timestamp)を返却
  - 適切なHTTPステータスコード(400: VALIDATION_ERROR、401: UNAUTHORIZED、403: FORBIDDEN、404: NOT_FOUND、409: CONFLICT、500: INTERNAL_ERROR)を返却
  - エラーレスポンスにスタックトレースやシステム内部情報を含めない
  - _Requirements: 6.8, 11.19, 11.20_

### Phase 6: テストとパフォーマンス検証

- [ ] 17. 単体テスト実装
- [ ] 17.1 (P) Domain層単体テスト実装
  - User Entity、Tweet Entityのパスワードハッシュ化、バリデーションをテスト
  - AuthenticationService、TimelineService、FollowServiceのビジネスロジックをテスト
  - JUnit 5 + Mockitoでリポジトリをモック
  - _Requirements: 10.7_

- [ ] 17.2 (P) Application層単体テスト実装
  - RegisterUserUseCase、LoginUserUseCase、CreateTweetUseCase、GetTimelineUseCase等のユースケースをテスト
  - トランザクション制御、エラーハンドリングを検証
  - JUnit 5 + Mockitoでリポジトリとドメインサービスをモック
  - _Requirements: 10.7_

- [ ] 18. 統合テスト実装
- [ ] 18.1 AuthController統合テスト実装
  - ユーザー登録・ログインAPIのエンドツーエンドテスト
  - TestContainersでPostgreSQL起動、実際のデータベースでテスト
  - RestAssuredでAPIテストを実行
  - _Requirements: 10.7_

- [ ] 18.2 (P) TweetController統合テスト実装
  - ツイート投稿・取得・削除APIのエンドツーエンドテスト
  - JWT認証フローを含むテスト
  - _Requirements: 10.7_

- [ ] 18.3 TimelineController統合テスト実装(N+1クエリ検証)
  - タイムライン取得APIのエンドツーエンドテスト
  - N+1クエリ問題が発生しないことを検証(クエリ実行回数をアサート)
  - ページネーション処理を検証
  - _Requirements: 3.5, 9.4, 10.7_

- [ ] 18.4 (P) Repository統合テスト実装
  - UserRepositoryImpl、TweetRepositoryImpl等のデータアクセステスト
  - TestContainersでPostgreSQL起動、実際のデータベースでテスト
  - _Requirements: 10.7_

- [ ] 18.5 (P) JWT認証フィルター統合テスト実装
  - JWT認証フローのエンドツーエンドテスト
  - トークン検証失敗時の401 Unauthorizedエラーを検証
  - _Requirements: 6.1, 6.2, 6.3, 10.7_

- [ ] 19. E2Eテスト実装
- [ ] 19.1 主要ユーザーフローE2Eテスト実装
  - ユーザー登録→ログイン→ツイート投稿→タイムライン表示フロー
  - ユーザーフォロー→タイムライン更新フロー
  - ツイートいいね・リツイート→カウント更新フロー
  - 検索機能フロー(ユーザー検索、ツイート検索)
  - エラーハンドリングフロー(認証エラー、バリデーションエラー)
  - _Requirements: 10.7_

- [ ] 20. パフォーマンステスト実装
- [ ] 20.1 パフォーマンステスト実行と検証
  - タイムライン取得のレスポンスタイムが95パーセンタイル500ms以下であることを検証
  - 毎秒100リクエストの処理(スループット)を検証
  - N+1クエリ問題の検証(タイムライン取得時のクエリ実行回数を1回のみであることを確認)
  - データベース接続プール枯渇テスト(同時接続数の上限をテスト)
  - JMeter/Gatlingでパフォーマンステストを実行
  - _Requirements: 9.1, 9.2, 9.4, 9.5_

### Phase 7: 可用性・保守性機能実装

- [ ] 21. ヘルスチェックとログ設定
- [ ] 21.1 (P) Spring Boot Actuator設定
  - /actuator/healthエンドポイントでヘルスチェックを提供
  - データベース接続、JVMメモリ使用率、ディスク使用率を監視
  - メトリクス(CPU使用率、メモリ使用率、レスポンスタイム、エラー率)を収集
  - _Requirements: 10.1, 10.6_

- [ ] 21.2 (P) Logback構造化ログ設定
  - Logbackで構造化ログ(JSON形式)を出力
  - ログレベル(INFO、WARN、ERROR)で適切にログ出力
  - パスワード、JWTトークンはログに出力しない(マスキング設定)
  - ログローテーション(日次、7日間保持)を設定
  - _Requirements: 10.2, 10.3, 10.4_

---

## タスク統計

- **合計メジャータスク**: 21
- **合計サブタスク**: 77
- **カバーされた要件**: 全11カテゴリ、189個の受け入れ基準すべて
- **平均サブタスクサイズ**: 1-3時間
- **並列実行可能タスク**: 52タスク (P)マーク付き

---

## 実装順序の原則

1. **依存関係の尊重**: オニオンアーキテクチャの依存関係に従い、内側(Domain)から外側(Presentation)へ
2. **インクリメンタルな進行**: 各タスクは前のタスクの出力を基に構築
3. **早期検証**: コア機能(認証、ツイート投稿)を早期にテストし、統合の健全性を確認
4. **並列実行の活用**: (P)マーク付きタスクは並列実行可能(データ依存なし、ファイル競合なし)
5. **テストの組み込み**: 各フェーズ完了後に単体テスト・統合テストを実施

---

## 次のステップ

1. **タスクレビュー**: すべてのタスクが要件をカバーしていることを確認
2. **実装開始**: `/kiro:spec-impl chirper-backend 1.1` で最初のタスクを実行
3. **テストカバレッジ95%達成**: 各タスク完了時に単体テスト・統合テストを実施
4. **パフォーマンス検証**: Phase 6でパフォーマンス要件(95パーセンタイル500ms以下)を検証
