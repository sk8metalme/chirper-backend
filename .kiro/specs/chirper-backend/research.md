# Research & Design Decisions

---
**Purpose**: chirper-backend の技術調査と設計判断の記録

**Usage**:
- 親プロジェクトのアーキテクチャ設計との整合性を確認
- 技術スタックの最新情報を収集
- Backend REST API としての責務範囲を明確化
---

## Summary

- **Feature**: chirper-backend
- **Discovery Scope**: Extension（親プロジェクトのアーキテクチャ実装）
- **Key Findings**:
  - Spring Boot 3.4.13が最新安定版だが、3.5.x/4.0.xへのアップグレードが推奨される
  - jjwt 0.13.0が最新バージョンでSpring Boot 3との互換性が確認された
  - PostgreSQL JDBC Driver 42.7.6がPostgreSQL 17をサポート

## Research Log

### Spring Boot バージョン選択

- **Context**: 親プロジェクトのアーキテクチャ設計書でSpring Boot 3.4.xが指定されているが、最新状況の確認が必要
- **Sources Consulted**:
  - [Spring Boot 3.4.13 Release](https://spring.io/blog/2025/12/18/spring-boot-3-4-13-available-now/)
  - [Spring Boot Release Notes](https://spring.io/blog/category/releases/)
- **Findings**:
  - Spring Boot 3.4.13が2025年12月18日にリリースされた
  - 3.4.xのオープンソースサポートは終了
  - 3.5.9および4.0.1が推奨バージョンとして提供されている
- **Implications**:
  - 初期実装は親プロジェクトとの整合性を保つため3.4.13を使用
  - 将来的に3.5.xまたは4.0.xへのアップグレードパスを考慮
  - 設計書にアップグレード戦略を含める

### JWT認証ライブラリ

- **Context**: JWT認証にjjwtライブラリを使用する方針だが、最新バージョンの確認が必要
- **Sources Consulted**:
  - [jjwt GitHub Repository](https://github.com/jwtk/jjwt)
  - [jjwt Maven Repository](https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api)
- **Findings**:
  - jjwt 0.13.0が最新バージョン（2025年8月20日リリース）
  - Spring Boot 3との互換性が確認されている
  - モジュラー依存関係（jjwt-api、jjwt-impl、jjwt-jackson）が必要
- **Implications**:
  - jjwt 0.13.0を使用
  - 3つのモジュール依存関係を設定に含める
  - Spring Securityのビルトインサポートも選択肢だが、jjwtの柔軟性を優先

### PostgreSQL JDBC Driver

- **Context**: PostgreSQL 17との互換性確認
- **Sources Consulted**:
  - [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
  - [PostgreSQL JDBC Driver 42.7.6 Release](https://jdbc.postgresql.org/changelogs/2025-05-28-42/)
- **Findings**:
  - PostgreSQL JDBC Driver 42.7.6がPostgreSQL 17をサポート
  - Spring Boot 3との互換性が確認されている
  - 自動ロード機能（Java Service Provider）により追加設定不要
- **Implications**:
  - PostgreSQL JDBC Driver 42.7.6を使用
  - application.propertiesでのデータソース設定のみで接続可能

### オニオンアーキテクチャ実装

- **Context**: 親プロジェクトで定義されたオニオンアーキテクチャをBackend Serviceに適用
- **Sources Consulted**:
  - `/Users/arigatatsuya/Work/git/multi-repo/docs/michi/chirper/overview/architecture.md`
- **Findings**:
  - 4層構造: Domain、Application、Infrastructure、Presentation
  - 依存関係の方向: 外側→内側（内側は外側に依存しない）
  - Repository InterfaceはDomain層で定義、Infrastructure層で実装
- **Implications**:
  - パッケージ構成を4層構造で設計
  - Domain層を純粋なJavaクラスとして実装（外部依存なし）
  - 依存性逆転の原則を厳格に適用

## Architecture Pattern Evaluation

| Option | Description | Strengths | Risks / Limitations | Notes |
|--------|-------------|-----------|---------------------|-------|
| Onion Architecture | ドメイン駆動設計に基づく4層構造 | 明確な責務分離、テスト容易性、保守性 | 学習曲線、初期設定の複雑さ | 親プロジェクトで採用決定済み |
| Layered Architecture | 従来の3層アーキテクチャ | シンプル、習得容易 | ドメインロジックの散在リスク | 不採用（親プロジェクトの方針に反する） |
| Clean Architecture | オニオンアーキテクチャの拡張 | より厳格な分離 | 過度な抽象化のリスク | オニオンで十分と判断 |

## Design Decisions

### Decision: Spring Boot バージョン選択

- **Context**: 親プロジェクトで3.4.xが指定されているが、最新情報では3.5.x/4.0.xが推奨される
- **Alternatives Considered**:
  1. Spring Boot 3.4.13 — 親プロジェクトとの整合性を保つ
  2. Spring Boot 3.5.9 — 最新の推奨バージョン
  3. Spring Boot 4.0.1 — 最新メジャーバージョン
- **Selected Approach**: Spring Boot 3.4.13を使用し、将来的に3.5.xへのアップグレードパスを準備
- **Rationale**:
  - 親プロジェクトのアーキテクチャ設計との整合性を優先
  - 3.4.13は安定版として実績がある
  - アップグレードパスを設計に含めることで将来対応を可能にする
- **Trade-offs**:
  - メリット: 親プロジェクトとの一貫性、既知の問題の回避
  - デメリット: オープンソースサポート終了により、商用サポートまたはアップグレードが必要
- **Follow-up**: Phase 1実装後にSpring Boot 3.5.xへのアップグレード検証を実施

### Decision: JWT実装ライブラリ

- **Context**: JWT認証の実装にjjwtとSpring Security Built-inの2つの選択肢がある
- **Alternatives Considered**:
  1. jjwt 0.13.0 — 柔軟なJWTライブラリ
  2. Spring Security Built-in — フレームワーク標準機能
- **Selected Approach**: jjwt 0.13.0を使用
- **Rationale**:
  - トークン生成・検証のカスタマイズが容易
  - 親プロジェクトのアーキテクチャ設計書で採用が示唆されている
  - 実績が豊富で、コミュニティサポートが充実
- **Trade-offs**:
  - メリット: 柔軟性、カスタマイズ性
  - デメリット: 追加依存関係
- **Follow-up**: JWT署名アルゴリズムの選定（HS256またはRS256）

### Decision: データベースマイグレーション戦略

- **Context**: データベーススキーマの管理にFlywayまたはLiquibaseを選択
- **Alternatives Considered**:
  1. Flyway — シンプルなSQLベースのマイグレーション
  2. Liquibase — XMLベースの高機能マイグレーション
- **Selected Approach**: Flywayを使用
- **Rationale**:
  - 親プロジェクトの要件定義でFlywayが指定されている
  - SQLファイルベースでシンプル
  - Spring Bootとの統合が容易
- **Trade-offs**:
  - メリット: シンプル、学習コストが低い
  - デメリット: Liquibaseと比較して機能が限定的
- **Follow-up**: マイグレーションファイルの命名規則とバージョニング戦略の確立

## Risks & Mitigations

- **Spring Boot 3.4.xのサポート終了** — アップグレードパスを設計に含め、Phase 2でSpring Boot 3.5.xへの移行を検討
- **N+1クエリ問題** — タイムライン取得時にJOIN FETCHまたは@EntityGraphを使用してイーガーロードを実装
- **JWT署名鍵の管理** — 環境変数または外部シークレット管理サービスで鍵を管理、ハードコーディングを禁止
- **データベース接続プール枯渇** — HikariCPの適切な設定（最小10、最大50）とモニタリングを実施
- **トランザクション境界の不明瞭さ** — Application層でトランザクション制御を明確化、@Transactionalアノテーションの適切な使用

## References

- [Spring Boot 3.4.13 Release](https://spring.io/blog/2025/12/18/spring-boot-3-4-13-available-now/)
- [Spring Boot Release Notes](https://spring.io/blog/category/releases/)
- [jjwt GitHub Repository](https://github.com/jwtk/jjwt)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
- [PostgreSQL JDBC Driver 42.7.6 Release](https://jdbc.postgresql.org/changelogs/2025-05-28-42/)
- 親プロジェクト要件定義: `/Users/arigatatsuya/Work/git/multi-repo/docs/michi/chirper/overview/requirements.md`
- 親プロジェクトアーキテクチャ設計: `/Users/arigatatsuya/Work/git/multi-repo/docs/michi/chirper/overview/architecture.md`
