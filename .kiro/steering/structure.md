# Project Structure

## Organization Philosophy

**Onion Architecture** (オニオンアーキテクチャ) - ドメイン駆動設計に基づく4層構造

依存関係の方向: 外側の層→内側の層(内側は外側に依存しない)

```
Presentation Layer (REST Controllers, Filters, Exception Handlers)
    ↓
Application Layer (Use Cases, Transaction Control)
    ↓
Domain Layer (Entities, Value Objects, Domain Services, Repository Interfaces)
    ↑
Infrastructure Layer (Repository Implementations, JPA Entities, External Services)
```

## Directory Patterns

### Domain Layer (`src/main/java/com/chirper/domain/`)
**Purpose**: ビジネスロジックとビジネスルールを集約、外部依存なしの純粋なJavaクラス
**Contains**:
- **Entities**: User、Tweet、Follow、Like、Retweet
- **Value Objects**: UserId、Email、Password、Username、TweetContent、TweetId
- **Domain Services**: AuthenticationService、TimelineService、FollowService
- **Repository Interfaces**: IUserRepository、ITweetRepository、IFollowRepository、ILikeRepository、IRetweetRepository

**Example**:
```java
// Value Object
record Username(String value) {
    public Username {
        if (value == null || value.length() < 3 || value.length() > 20) {
            throw new IllegalArgumentException("Username must be 3-20 characters");
        }
    }
}

// Entity
class User {
    private UserId id;
    private Username username;
    private Email email;
    private Password password;
    // ...
}
```

### Application Layer (`src/main/java/com/chirper/application/`)
**Purpose**: ユースケース実装、トランザクション制御(`@Transactional`)
**Contains**:
- RegisterUserUseCase、LoginUserUseCase、UpdateProfileUseCase
- CreateTweetUseCase、DeleteTweetUseCase
- GetTimelineUseCase、FollowUserUseCase、LikeTweetUseCase、RetweetUseCase
- SearchUseCase

**Example**:
```java
@Service
@Transactional
public class RegisterUserUseCase {
    public Result<UserResponse, ErrorResponse> execute(RegisterUserRequest request) {
        // ユーザー名重複チェック → User Entity生成 → Repository保存
    }
}
```

### Infrastructure Layer (`src/main/java/com/chirper/infrastructure/`)
**Purpose**: データアクセス実装、外部サービス連携
**Contains**:
- **Repository Implementations**: UserRepositoryImpl、TweetRepositoryImpl等
- **JPA Entities**: UserJpaEntity、TweetJpaEntity等(Domain EntityとJPA Entityを分離)
- **Persistence**: Spring Data JPA Repository、Flyway Migrations

**Example**:
```java
@Repository
public class UserRepositoryImpl implements IUserRepository {
    @Autowired
    private UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity = toJpaEntity(user);
        UserJpaEntity saved = jpaRepository.save(jpaEntity);
        return toDomainEntity(saved);
    }
}
```

### Presentation Layer (`src/main/java/com/chirper/presentation/`)
**Purpose**: REST APIエンドポイント、リクエスト/レスポンスモデル
**Contains**:
- **Controllers**: AuthController、UserController、TweetController、TimelineController、SocialController、SearchController
- **Filters**: JwtAuthenticationFilter
- **Exception Handlers**: GlobalExceptionHandler
- **DTOs**: Request/Response models

**Example**:
```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        // RegisterUserUseCaseを呼び出し
    }
}
```

## Naming Conventions

- **Packages**: com.chirper.{layer}.{domain} (例: com.chirper.domain.user、com.chirper.application.auth)
- **Classes**:
  - Entities: PascalCase (User、Tweet)
  - Value Objects: PascalCase + record (UserId、Email)
  - Use Cases: {Action}{Entity}UseCase (RegisterUserUseCase)
  - Controllers: {Entity}Controller (AuthController)
  - Repository Interfaces: I{Entity}Repository (IUserRepository)
  - Repository Implementations: {Entity}RepositoryImpl (UserRepositoryImpl)
- **Methods**: camelCase、動詞で始める (save、findByUsername、execute)

## Code Organization Principles

1. **依存性逆転の原則**: Domain層でRepository Interfaceを定義、Infrastructure層で実装
2. **単一責任の原則**: 各Use Caseは1つのユースケースのみを実装
3. **Domain EntityとJPA Entityの分離**: Domain層は純粋なJavaクラス、Infrastructure層でJPA依存のEntityを管理
4. **トランザクション境界**: Application層(`@Transactional`)でトランザクション制御、Controller層は関与しない
5. **ビジネスロジックの集約**: Domain層にビジネスルールを集約、Value Objectのコンストラクタでバリデーション

## Database Migrations (`src/main/resources/db/migration/`)

Flywayでバージョン管理、SQLファイルベース

**Naming**: `V{version}__{description}.sql` (例: V1__create_users_table.sql)

---
_Golden Rule: 新しいコードが既存パターンに従っていれば、このドキュメントの更新は不要_
