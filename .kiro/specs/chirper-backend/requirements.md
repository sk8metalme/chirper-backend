# Requirements Document

## Project Description (Input)
chirper backend - Spring Boot REST API with Onion Architecture

## Introduction

chirper-backendは、Twitter風SNSアプリケーション「Chirper」のバックエンドサービスです。Spring Boot 3.xをベースとしたREST APIサーバーとして、ユーザー管理、ツイート管理、タイムライン、ソーシャル機能などのビジネスロジックを提供します。オニオンアーキテクチャ（Onion Architecture）を採用し、ドメイン駆動設計の原則に基づいて構築されます。

**親プロジェクト参照**:
- 要件定義: `/Users/arigatatsuya/Work/git/multi-repo/docs/michi/chirper/overview/requirements.md`
- アーキテクチャ: `/Users/arigatatsuya/Work/git/multi-repo/docs/michi/chirper/overview/architecture.md`

**Backend Serviceの責務範囲**:
- Spring Boot REST APIサーバー
- JWT認証・認可
- ユーザー管理（登録、ログイン、プロフィール管理）
- ツイート管理（投稿、取得、削除）
- タイムライン機能（フォローユーザーのツイート取得）
- ソーシャル機能（フォロー、いいね、リツイート）
- 検索機能（ユーザー・ツイート検索）
- PostgreSQLデータベース連携
- オニオンアーキテクチャ実装（Domain、Application、Infrastructure、Presentation層）

## Requirements

### Requirement 1: ユーザー登録・認証
**Objective:** ユーザーとして、アカウントを登録し、安全にログインできることで、Chirperの機能を利用できる

#### Acceptance Criteria
1. When ユーザーが新規登録フォームを送信する際、If ユーザー名が3文字以上20文字以下かつ一意である場合、the Backend Service shall アカウントを作成し、成功メッセージ（userId）を返却する
2. When ユーザー登録リクエストを受け取る際、If ユーザー名が既に存在する場合、the Backend Service shall 409 Conflictエラーを返却する
3. When ユーザーがログインリクエストを送信する際、If 認証情報（username、password）が正しい場合、the Backend Service shall JWTトークンを発行し、ユーザーID、ユーザー名、有効期限とともに返却する
4. When ログインリクエストを受け取る際、If 認証情報が誤っている場合、the Backend Service shall 401 Unauthorizedエラーを返却する
5. The Backend Service shall パスワードをbcryptでハッシュ化（ソルト付き、コスト係数10）して保存する
6. The Backend Service shall JWTトークンの有効期限を1時間とする
7. When ユーザーがプロフィール更新リクエストを送信する際、If JWTトークンが有効である場合、the Backend Service shall プロフィール情報（displayName、bio、avatarUrl）を更新する
8. When 認証済みユーザーがユーザープロフィール取得リクエストを送信する際、If ユーザー名が存在する場合、the Backend Service shall ユーザー情報（userId、username、displayName、bio、avatarUrl、createdAt）を返却する

### Requirement 2: ツイート管理
**Objective:** 認証済みユーザーとして、ツイートを投稿・取得・削除できることで、コンテンツを共有できる

#### Acceptance Criteria
1. When 認証済みユーザーがツイート投稿リクエストを送信する際、If ツイート本文が1文字以上280文字以下である場合、the Backend Service shall ツイートをデータベースに保存し、tweetId、createdAtを返却する
2. When ツイート投稿リクエストを受け取る際、If ツイート本文が280文字を超える場合、the Backend Service shall 400 Bad Requestエラーを返却する
3. When ツイートID指定の取得リクエストを受け取る際、If ツイートが存在しis_deleted=falseである場合、the Backend Service shall ツイート情報（tweetId、userId、content、createdAt、likesCount、retweetsCount）を返却する
4. When ツイート削除リクエストを受け取る際、If 投稿者本人である場合、the Backend Service shall is_deletedフラグをtrueに設定し（論理削除）、204 No Contentを返却する
5. When ツイート削除リクエストを受け取る際、If 投稿者本人でない場合、the Backend Service shall 403 Forbiddenエラーを返却する
6. The Backend Service shall 削除されたツイート（is_deleted=true）を取得APIで返却しない

### Requirement 3: タイムライン機能
**Objective:** 認証済みユーザーとして、フォローユーザーのツイートを時系列で閲覧できることで、最新情報を追跡できる

#### Acceptance Criteria
1. When 認証済みユーザーがタイムライン取得リクエストを送信する際、If フォローユーザーが存在する場合、the Backend Service shall フォローユーザーのツイートを新しい順（created_at DESC）で返却する
2. When タイムライン取得リクエストを受け取る際、the Backend Service shall ページネーション（page、size）に基づき、指定された件数のツイートを返却する
3. The Backend Service shall タイムラインレスポンスに、各ツイートの投稿者情報（username、displayName、avatarUrl）を含める
4. The Backend Service shall タイムラインレスポンスに、各ツイートのいいね数、リツイート数を含める
5. The Backend Service shall タイムライン取得時にN+1クエリ問題を回避する（JOIN FETCHまたは@EntityGraphを使用）
6. When タイムライン取得時、If ページサイズが指定されていない場合、the Backend Service shall デフォルトで20件を返却する
7. The Backend Service shall タイムラインレスポンスに総ページ数（totalPages）を含める

### Requirement 4: ソーシャル機能（フォロー・いいね・リツイート）
**Objective:** ユーザーとして、他のユーザーをフォローし、ツイートにいいね・リツイートできることで、ソーシャルな交流ができる

#### Acceptance Criteria - フォロー機能
1. When 認証済みユーザーが他ユーザーのフォローリクエストを送信する際、If フォロー対象ユーザーが存在しかつ未フォロー状態である場合、the Backend Service shall フォロー関係をfollowsテーブルに作成し、201 Createdを返却する
2. When フォローリクエストを受け取る際、If 既にフォロー済みである場合、the Backend Service shall 409 Conflictエラーを返却する
3. When フォロー解除リクエストを受け取る際、If フォロー関係が存在する場合、the Backend Service shall フォロー関係を削除し、204 No Contentを返却する
4. The Backend Service shall ユーザーが自分自身をフォローすることを禁止する（400 Bad Request）

#### Acceptance Criteria - いいね機能
5. When 認証済みユーザーがツイートのいいねリクエストを送信する際、If ツイートが存在しかつ未いいね状態である場合、the Backend Service shall いいねをlikesテーブルに記録し、201 Createdを返却する
6. When いいねリクエストを受け取る際、If 既にいいね済みである場合、the Backend Service shall 409 Conflictエラーを返却する
7. When いいね解除リクエストを受け取る際、If いいね記録が存在する場合、the Backend Service shall いいね記録を削除し、204 No Contentを返却する

#### Acceptance Criteria - リツイート機能
8. When 認証済みユーザーがツイートのリツイートリクエストを送信する際、If ツイートが存在しかつ未リツイート状態である場合、the Backend Service shall リツイートをretweetsテーブルに記録し、201 Createdを返却する
9. When リツイートリクエストを受け取る際、If 既にリツイート済みである場合、the Backend Service shall 409 Conflictエラーを返却する
10. When リツイート解除リクエストを受け取る際、If リツイート記録が存在する場合、the Backend Service shall リツイート記録を削除し、204 No Contentを返却する

### Requirement 5: 検索機能
**Objective:** ユーザーとして、ユーザー名やツイート本文でキーワード検索できることで、関心のあるコンテンツを発見できる

#### Acceptance Criteria
1. When ユーザーが検索リクエストを送信する際、If キーワードが2文字以上である場合、the Backend Service shall 関連するユーザー（username、displayNameが部分一致）とツイート（contentが部分一致）を検索し、結果を返却する
2. When 検索リクエストを受け取る際、If キーワードが2文字未満である場合、the Backend Service shall 400 Bad Requestエラーを返却する
3. The Backend Service shall 検索結果をページネーション（page、size）で返却する
4. The Backend Service shall 検索時に論理削除されたツイート（is_deleted=true）を結果から除外する

### Requirement 6: セキュリティ・認証認可
**Objective:** システムとして、不正アクセスやデータ漏洩を防止し、安全にサービスを提供できる

#### Acceptance Criteria
1. The Backend Service shall すべての保護されたエンドポイント（/api/v1/tweets、/api/v1/timeline等）へのアクセス時にJWTトークンを検証する
2. When JWTトークンの有効期限が切れている場合、the Backend Service shall 401 Unauthorizedエラーを返却する
3. When JWTトークンの署名が不正である場合、the Backend Service shall 401 Unauthorizedエラーを返却する
4. The Backend Service shall CORS設定でフロントエンドサービスのオリジンからのリクエストのみ許可する
5. The Backend Service shall すべてのリクエストパラメータをバリデーションする（Bean Validation使用）
6. The Backend Service shall SQLインジェクション対策としてJPAのパラメータバインディングを使用する
7. The Backend Service shall セキュリティヘッダー（Content-Security-Policy、X-Frame-Options、X-Content-Type-Options、Strict-Transport-Security）を設定する
8. The Backend Service shall エラーレスポンスにスタックトレースやシステム内部情報を含めない

### Requirement 7: オニオンアーキテクチャ実装
**Objective:** システムとして、保守性とテスタビリティの高いアーキテクチャを実現する

#### Acceptance Criteria
1. The Backend Service shall Domain層、Application層、Infrastructure層、Presentation層の4層構造で実装する
2. The Backend Service shall Domain層にビジネスロジックとビジネスルールを集約する（Entity、Value Object、Domain Service、Repository Interface）
3. The Backend Service shall Application層にユースケースを実装し、トランザクション制御を行う
4. The Backend Service shall Infrastructure層にデータアクセス（JPA Repository実装）と外部サービス連携を実装する
5. The Backend Service shall Presentation層にREST Controllerとリクエスト/レスポンスモデルを実装する
6. The Backend Service shall 依存関係の方向を「外側の層→内側の層」とし、内側の層は外側の層に依存しない
7. The Backend Service shall Domain層のRepository InterfaceをInfrastructure層で実装する（依存性逆転の原則）

### Requirement 8: データベース設計
**Objective:** システムとして、データの整合性を保ち、効率的にデータを管理する

#### Acceptance Criteria
1. The Backend Service shall PostgreSQL 16以上を使用する
2. The Backend Service shall Flywayでデータベースマイグレーションを管理する
3. The Backend Service shall users、tweets、follows、likes、retweetsの5つのテーブルを作成する
4. The Backend Service shall 主キーにUUIDを使用する
5. The Backend Service shall usernameとemailにUNIQUE制約を設定する
6. The Backend Service shall 外部キー制約を設定し、参照整合性を保証する
7. The Backend Service shall インデックスを適切に設定する（username、email、user_id+created_at、follower_user_id+followed_user_id等）
8. The Backend Service shall カスケード削除を設定する（ユーザー削除時、関連するツイート・フォロー関係・いいね・リツイートを削除）
9. The Backend Service shall 日時データをUTC（協定世界時）で保存する
10. The Backend Service shall 文字エンコーディングにUTF-8を使用する

### Requirement 9: パフォーマンス要件
**Objective:** システムとして、高速なレスポンスを提供し、ユーザー体験を向上させる

#### Acceptance Criteria
1. The Backend Service shall API呼び出しの95パーセンタイルレスポンスタイムを500ms以下とする
2. The Backend Service shall 毎秒100リクエストを処理可能とする
3. The Backend Service shall データベース接続プールを適切に設定する（HikariCP: 最小10、最大50）
4. The Backend Service shall タイムライン取得時にN+1クエリ問題を回避する
5. The Backend Service shall ページネーションでデータ取得量を制限する（デフォルト20件、最大100件）

### Requirement 10: 可用性・保守性要件
**Objective:** システムとして、安定稼働し、問題発生時に迅速に対応できる

#### Acceptance Criteria
1. The Backend Service shall `/actuator/health` エンドポイントでヘルスチェックを提供する
2. The Backend Service shall ログをJSON形式で出力する（構造化ログ）
3. The Backend Service shall ログレベル（INFO、WARN、ERROR）で適切にログ出力する
4. The Backend Service shall パスワード、JWTトークンをログに出力しない
5. The Backend Service shall エラー発生時にリクエストID、エラーコード、エラーメッセージ、タイムスタンプを含むエラーレスポンスを返却する
6. The Backend Service shall Spring Boot Actuatorでメトリクス（CPU使用率、メモリ使用率、レスポンスタイム、エラー率）を収集する
7. The Backend Service shall 単体テストカバレッジ95%以上を達成する

### Requirement 11: API仕様
**Objective:** Frontend Serviceとして、標準化されたREST APIで通信し、一貫性のあるインターフェースを利用できる

#### Acceptance Criteria - 認証API
1. The Backend Service shall `POST /api/v1/auth/register` エンドポイントでユーザー登録を受け付ける（リクエスト: username、email、password、レスポンス: userId、message）
2. The Backend Service shall `POST /api/v1/auth/login` エンドポイントでユーザー認証を受け付ける（リクエスト: username、password、レスポンス: token、userId、username、expiresAt）

#### Acceptance Criteria - ツイート管理API
3. The Backend Service shall `POST /api/v1/tweets` エンドポイントでツイート投稿を受け付ける（認証必須、リクエスト: content、mediaUrls、レスポンス: tweetId、createdAt）
4. The Backend Service shall `GET /api/v1/tweets/{tweetId}` エンドポイントでツイート取得を提供する（レスポンス: tweetId、userId、content、createdAt、likesCount、retweetsCount）
5. The Backend Service shall `DELETE /api/v1/tweets/{tweetId}` エンドポイントでツイート削除を受け付ける（認証必須、投稿者のみ）

#### Acceptance Criteria - タイムラインAPI
6. The Backend Service shall `GET /api/v1/timeline` エンドポイントでタイムライン取得を提供する（認証必須、クエリパラメータ: page、size、レスポンス: tweets配列（各ツイートにlikedByCurrentUser、retweetedByCurrentUserを含む）、totalPages）

#### Acceptance Criteria - ユーザープロフィールAPI
7. The Backend Service shall `GET /api/v1/users/{username}` エンドポイントでユーザープロフィール取得を提供する（認証必須、レスポンス: userId、username、displayName、bio、avatarUrl、createdAt、followersCount、followingCount、followedByCurrentUser、userTweets）
8. The Backend Service shall `PUT /api/v1/users/profile` エンドポイントでプロフィール更新を受け付ける（認証必須、リクエスト: displayName、bio、avatarUrl）

#### Acceptance Criteria - ソーシャル機能API
9. The Backend Service shall `POST /api/v1/users/{userId}/follow` エンドポイントでユーザーフォローを受け付ける（認証必須）
10. The Backend Service shall `DELETE /api/v1/users/{userId}/follow` エンドポイントでユーザーフォロー解除を受け付ける（認証必須）
11. The Backend Service shall `POST /api/v1/tweets/{tweetId}/like` エンドポイントでツイートいいねを受け付ける（認証必須）
12. The Backend Service shall `DELETE /api/v1/tweets/{tweetId}/like` エンドポイントでツイートいいね解除を受け付ける（認証必須）
13. The Backend Service shall `POST /api/v1/tweets/{tweetId}/retweet` エンドポイントでリツイートを受け付ける（認証必須）
14. The Backend Service shall `DELETE /api/v1/tweets/{tweetId}/retweet` エンドポイントでリツイート解除を受け付ける（認証必須）

#### Acceptance Criteria - データフォーマット
15. The Backend Service shall すべてのAPIリクエスト・レスポンスでJSON形式を使用する（Content-Type: application/json）
16. The Backend Service shall 日時をISO 8601形式で返却する（例: 2025-12-22T13:32:36.475Z）
17. The Backend Service shall 文字エンコーディングにUTF-8を使用する
18. The Backend Service shall 認証が必要なエンドポイントでBearer Token（Authorization: Bearer {token}）を使用する

#### Acceptance Criteria - エラーレスポンス
19. The Backend Service shall エラーレスポンスに統一形式を使用する（error.code、error.message、error.details、error.timestamp）
20. The Backend Service shall 適切なHTTPステータスコードを返却する（400: VALIDATION_ERROR、401: UNAUTHORIZED、403: FORBIDDEN、404: NOT_FOUND、409: CONFLICT、500: INTERNAL_ERROR）
