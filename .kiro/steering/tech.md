# Technology Stack

## Architecture

**Onion Architecture** (オニオンアーキテクチャ)
- 4層構造: Domain、Application、Infrastructure、Presentation
- 依存関係の方向: 外側→内側(内側は外側に依存しない)
- Repository InterfaceはDomain層で定義、Infrastructure層で実装(依存性逆転の原則)
- Domain層は純粋なJavaクラス(外部依存なし)

## Core Technologies

- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.4.13 (将来的に3.5.xへアップグレード検討)
- **Runtime**: Java 21+ (LTS版)
- **Database**: PostgreSQL 17.x
- **Migration**: Flyway 10.x
- **Security**: Spring Security 6.3.x + jjwt 0.13.0 (JWT生成・検証)
- **ORM**: Spring Data JPA 3.3.x + Hibernate 6.5.x
- **Connection Pool**: HikariCP 5.x (最小10、最大50)
- **Build Tool**: Gradle 8.x

## Key Libraries

- **jjwt**: JWT生成・検証(Spring Securityビルトイン機能より柔軟性が高い)
- **Spring Boot Actuator**: ヘルスチェック、メトリクス収集
- **Logback**: 構造化ログ(JSON形式)、パスワード・JWT等のマスキング
- **TestContainers**: 統合テストでPostgreSQL起動

## Development Standards

### Type Safety
- Value Object設計: UserId、Email、Password、Username、TweetContent等をrecordで実装
- ビジネスルールをValue Objectのコンストラクタで強制
- Domain層は純粋なJavaクラス(Spring依存なし)

### Code Quality
- Bean Validation: `@Valid`、`@Size`、`@Email`、`@NotNull`制約
- エラーハンドリング: 統一されたエラーレスポンス形式(error.code、error.message、error.details、error.timestamp)
- セキュリティ: パスワードハッシュ化(bcrypt、コスト係数10)、JWTトークン検証、SQLインジェクション対策(JPAパラメータバインディング)

### Testing
- **単体テスト**: JUnit 5 + Mockito、Domain層・Application層のビジネスロジックをカバー
- **統合テスト**: RestAssured + TestContainers、N+1クエリ問題の検証
- **E2Eテスト**: 主要ユーザーフローのエンドツーエンドテスト
- **パフォーマンステスト**: JMeter/Gatling、95パーセンタイル500ms以下を検証
- **カバレッジ目標**: 95%以上

## Development Environment

### Required Tools
- Java 21 LTS
- Gradle 8.x
- PostgreSQL 17.x (ローカル開発環境)
- Docker (TestContainers用)

### Common Commands
```bash
# Dev: ./gradlew bootRun
# Build: ./gradlew build
# Test: ./gradlew test
# Migration: ./gradlew flywayMigrate
```

## Key Technical Decisions

1. **Spring Boot 3.4.13選定**: 親プロジェクトとの整合性を保ちつつ、安定版を採用。オープンソースサポート終了を考慮し、3.5.xへのアップグレードパスを設計に含める
2. **jjwt 0.13.0選定**: Spring Securityビルトイン機能と比較して、トークンのカスタマイズ性が高く、実績が豊富
3. **PostgreSQL 17.x選定**: ACID特性、インデックス最適化、JSON型サポート、成熟したエコシステム
4. **Flyway選定**: Liquibaseと比較してシンプルで学習コストが低く、SQLファイルベースで可読性が高い
5. **N+1クエリ対策**: JOIN FETCHまたは@EntityGraphでイーガーロード、統合テストでクエリ実行回数を検証

---
_Phase 2検討事項: Redisキャッシュ、リードレプリカ、画像アップロード機能、リアルタイム通知(WebSocket/SSE)_
