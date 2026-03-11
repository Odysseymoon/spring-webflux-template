## Spring WebFlux REST Template (Kotlin/Gradle)
---
Spring WebFlux 기반 REST API 템플릿입니다. Kotlin + Gradle(Kotlin DSL) 기준으로 구성되어 있습니다.

>Framework 및 개발환경
- Java 17+
- Kotlin 1.9.x
- Spring Boot 3.3.x
- Spring WebFlux
- Spring Data JPA & QueryDSL (Jakarta)
- Spring Cache with Valkey
- Spring Security with JWT
- SpringDoc OpenAPI (Swagger UI)
- Jasypt (암복호화)
- Gradle (Kotlin DSL)
- MySQL 8.x & Valkey 7.2.x with Docker

---
>프로젝트 구조
```text
.
├─ build.gradle.kts
├─ settings.gradle.kts
├─ gradlew / gradlew.bat / gradle/wrapper
├─ src
│  ├─ main
│  │  ├─ kotlin/moon/odyssey/webflux
│  │  │  ├─ WebfluxApplication.kt
│  │  │  ├─ application
│  │  │  │  ├─ auth (entity, repository, rest, service, support, model)
│  │  │  │  ├─ ping/rest
│  │  │  │  └─ advice (GlobalAdvice)
│  │  │  ├─ config (WebFluxConfig, SecurityConfig, ValkeyConfig, WebClientConfig, AsyncConfig, ...)
│  │  │  ├─ config/properties (AuthJwtProperties, AsyncProperties, SchedulingProperties)
│  │  │  └─ utils (converter, reactive, exception, lambda, code)
│  │  └─ resources
│  │     ├─ application.yaml
│  │     └─ i18n/messages*.properties
│  └─ test
│     ├─ kotlin/moon/odyssey/webflux (tests)
│     └─ resources/application.yaml, data.sql
```

---
>빌드 & 테스트
```bash
./gradlew test
./gradlew bootRun
./gradlew clean build
```

---
>기본 Configuration
`src/main/resources/application.yaml`에서 로컬/운영 프로파일을 구성합니다.
테스트 프로파일은 `src/test/resources/application.yaml`에서 H2와 simple cache를 사용합니다.

```yaml
spring:
  profiles:
    active: local
  datasource:
    hikari:
      maximum-pool-size: 10
  jpa:
    open-in-view: false
  messages:
    basename: i18n/messages

auth:
  jwt:
    secretKey: mySecretKey
    issuer: odysseymoon
    expireDays: 30
```

---
>Annotated Controllers vs Functional Endpoints
Spring WebFlux는 Annotation 기반 컨트롤러와 Functional Endpoints를 모두 지원합니다.
이 템플릿은 Validation, OpenAPI 문서화 등을 고려해 Annotated Controller 방식을 사용합니다.

---
>WebFlux 기본 설정
`src/main/kotlin/moon/odyssey/webflux/config/WebFluxConfig.kt`
```kotlin
@Configuration
class WebFluxConfig(
    @Value("\${spring.datasource.hikari.maximum-pool-size:10}")
    private val connectionPoolSize: Int
) {
    @Bean("jdbcScheduler")
    fun jdbcScheduler(): Scheduler =
        Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize))

    @Bean
    fun transactionTemplate(transactionManager: PlatformTransactionManager): TransactionTemplate =
        TransactionTemplate(transactionManager)
}
```

---
>암복호화 (Jasypt)
`build.gradle.kts`
```kotlin
implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")
implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
```

`src/main/kotlin/moon/odyssey/webflux/config/EncryptConfig.kt`
```kotlin
@Bean("jasyptStringEncryptor")
fun stringEncryptor(): StringEncryptor {
    val config = SimpleStringPBEConfig().apply {
        password = password
        algorithm = "PBEWithSHA1AndDESede"
        keyObtentionIterations = 1000
        poolSize = 1
        provider = BouncyCastleProvider()
        setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator")
        stringOutputType = "base64"
    }
    return PooledPBEStringEncryptor().apply { setConfig(config) }
}
```

`src/main/kotlin/moon/odyssey/webflux/utils/converter/StringEncryptConverter.kt`를 통해
JPA 필드에 자동 암복호화를 적용할 수 있습니다. (필요 시 엔티티에 `@Convert` 추가)

---
>Scheduling & Async
- 설정 prefix: `async.thread-pool.*`, `schedule.thread-pool.size`
- 구성 클래스: `AsyncConfig`, `SchedulingConfig`, `AsyncProperties`, `SchedulingProperties`

```yaml
async:
  thread-pool:
    size: 20
    max: 100
    keepAliveSeconds: 60
    queueCapacity: 1000

schedule:
  thread-pool:
    size: 30
```

---
>i18n & 에러 메시지 처리
- Locale: `src/main/kotlin/moon/odyssey/webflux/config/support/LocaleResolver.kt`
- Advice: `src/main/kotlin/moon/odyssey/webflux/application/advice/GlobalAdvice.kt`
- 메시지: `src/main/resources/i18n/messages*.properties`

```kotlin
@RestControllerAdvice
class GlobalAdvice(
    private val messageSource: MessageSource
) {
    @ExceptionHandler(AppBaseException::class)
    fun handleAppBaseException(
        ex: AppBaseException,
        locale: Locale,
        exchange: ServerWebExchange
    ): ResponseEntity<AppErrorResponse> { /* ... */ }
}
```

---
>Cache with Valkey
- 설정 클래스: `CacheConfig`, `ValkeyConfig`
- 로컬 Docker 구성은 `docker/docker-compose.yaml`의 `valkey/valkey:7.2-alpine` 이미지를 사용합니다.
- Spring Boot/Spring Data는 여전히 `spring.data.redis.*`, `spring.cache.redis.*`, `RedisTemplate` 계열 추상화를 사용합니다.
- `@Cacheable`은 Mono/Flux 직접 캐싱을 지원하지 않으므로 `RxUtil.cacheMono`를 사용합니다.

```kotlin
fun <T : Any> cacheMono(...): Mono<T> {
    return CacheMono.lookup(...)
        .onCacheMissResume(retriever)
        .andWriteWith(...)
}
```

---
>WebClient 설정
`src/main/kotlin/moon/odyssey/webflux/config/WebClientConfig.kt`에서
timeouts, SSL, logging, maxInMemorySize 등을 구성합니다.

---
>Spring Security + JWT
- Config: `SecurityConfig`, `AuthenticationJwtManager`, `BearerSecurityContextRepository`
- JWT properties: `auth.jwt.*` (`AuthJwtProperties`)

```kotlin
@Bean
fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
    http
        .httpBasic { it.disable() }
        .formLogin { it.disable() }
        .authenticationManager(authenticationManager)
        .securityContextRepository(securityContextRepository)
        .authorizeExchange { exchanges ->
            exchanges
                .pathMatchers("/api/ping/**", "/api/auth/sign**").permitAll()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/api/**").authenticated()
                .anyExchange().permitAll()
        }
        .build()
```

---
>OpenAPI
`OpenApiConfig`에서 `bearerAuth` 스키마를 등록합니다.
- Swagger UI: `/swagger-ui/index.html`

---
>Sample Endpoints
- `GET /api/ping`
- `POST /api/auth/signup`
- `POST /api/auth/signin`
- `GET /api/auth/refresh`
