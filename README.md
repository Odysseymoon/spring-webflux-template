## Spring WebFlux REST Template
---
Spring Webflux 사용을 위한 기본 Template을 정리하였습니다.

>Framework 및 개발환경
- Java 1.8 later
- Spring Boot 2.2.x
- Spring WebFlux
- Spring Data JPA & QueryDSL
- Spring Cache with Redis
- Spring Security with JWT
- MySQL 8.0.19 & Redis 5.0.8  with Docker

---
>Annotated Controllers vs Functional Endpoints

Spring WebFlux는 Spring MVC에서 사용하던 Annotation 기반에 `@Controller` 엔드포인트와 FP 기반의 Functional Endpoints의 두 가지 방식을 모두 다 지원합니다.
어느것을 사용해도 상관없지만 REST API에 필요한 다양한 설정 및 Validation 그리고 Documentation 을 위한 Swagger 설정 등에 편의성 때문에
Annotated Controller 방식을 사용하기로 합니다.

---
>기본 Configuration

- application.yaml 을 통해 아래와 같이 Spring Boot 기본 구성을 설정합니다.
```yaml
#기본 로깅 설정
logging:
  level:
    root: DEBUG
    org.springframework.web.reactive.function.client.ExchangeFunctions: DEBUG   #Webclient의 log()를 위해 설정
    org.hibernate.type.descriptor.sql: trace
  file:
    path: target/logs

#서버 port 설정
server:
  port: 8080

spring:
  profiles:
    active: local   # 기본 profile을 개발용 local로 설정
  datasource:
    hikari:
      minimum-idle: 3
      maximum-pool-size: 10
      connection-timeout: 30000
      idle-timeout: 600000
      validation-timeout: 40000
    sql-script-encoding: UTF-8
    initialization-mode: always   # local 개발시에만 schema.sql과 data.sql을 로딩하고 불필요시 never로 변경
  jpa:
    database: mysql
    database-platform: org.hibernate.dialect.MySQL8Dialect
    show-sql: true
    hibernate:
      ddl-auto: validate
    properties:
      hibernate.show_sql: true
      hibernate.use_sql_comments: true
      hibernate.format_sql: true
      hibernate.query.in_clause_parameter_padding: true   # Prepared Statement 성능 향상을 위해 Where IN Clause의 개수를 padding
    open-in-view: false     # REST용 Framework이므로 OSIV 해제
  jackson:
    default-property-inclusion: non_empty
    serialization:
      WRITE_DATES_AS_TIMESTAMPS: false    # Jackson Serialization 시 LocalDateTime 등 타입 출력 ISO 8601 포멧으로 변경
```

- `WebFluxConfig` 를 통해 아래와 같이 WebFlux 기본 설정을 Override 합니다.
```java
@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int connectionPoolSize;

    //Swagger 설정을 위한 static resource 설정
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html**")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    //WebFlux를 아직 지원하지 않는 MySQL에 Transaction 처리를 위해 별도 Scheduler 설정
    @Bean("jdbcScheduler")
    public Scheduler jdbcScheduler() {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
    }

    //WebFlux 상에서 JPA LazyInitialize 문제가 발생 시 직접 Transaction을 관리하기 위한 template 로딩
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
```

---
>암호화 설정

Property나 Database 등에서 양방향 암복호화를 위한 Encryption 설정을 아래와 같이 구성합니다.

- `pom.xml` 에 암호화 관련 라이브러리를 추가합니다 (jasypt-spring-boot-starter 활용)
```xml
<!-- Encryption Library -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.2</version>
</dependency>
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.64</version>
</dependency>
```

- `EncryptConfig`를 통해 암호화 키 및 알고리즘 등을 설정합니다. 
```java
@Configuration
public class EncryptConfig {

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("{CUSTOM_PASSWORD}");    //적용할 패스워드를 넣는다 
        config.setAlgorithm("PBEWithSHA1AndDESede");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProvider(new BouncyCastleProvider());
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }
}
```

- Application에서 위에서 정의한 빈을 통해 별도 암복호화가 가능하고 
```java
public class EncryptConfigTest {

    @Autowired
    private StringEncryptor jasyptStringEncryptor;

    @Test
    public void testEncrypt() {
        String originString = "EncryptConfigTest";

        String encryptedString = jasyptStringEncryptor.encrypt(originString);
        log.info("##### encrypted string : {}", encryptedString);
        
        String decryptedString = jasyptStringEncryptor.decrypt(encryptedString); 
        log.info("##### decrypted string : {}", decryptedString);
    }

}
```
- property file도 암호화된 문자열을 `ENC()` 형태로 감싸서 적용할 수 있습니다.
```yaml
spring:
  profiles: local
  datasource:
    url: jdbc:mysql://localhost:13306/testDB?useUnicode=yes&characterEncoding=UTF-8
    username: ENC(iz8p6xZ6Or+gbMphJu8VsHIHwNGKNgVW)     #암호화된 유저정보 
    password: ENC(tDUfykZXyTthimgZT35ECw+GpX0y/TZz)     #암호화된 패스워드
    driver-class-name: com.mysql.cj.jdbc.Driver
``` 
- JPA Entity에 암복호화가 필요한 필드에 대해서도 `AttributeConverter`를 통해 자동 암복호화가 가능합니다.

아래와 같이 AttributeConverter 를 정의하고 
```java
@Converter
public class StringEncryptConverter implements AttributeConverter<String, String> {

    private static StringEncryptor stringEncryptor;

    @Autowired
    @Qualifier("jasyptStringEncryptor")
    public void setStringEncryptor(StringEncryptor encryptor) {
        StringEncryptConverter.stringEncryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(String entityString) {

        return Optional.ofNullable(entityString)
                       .filter(s -> !s.isEmpty())
                       .map(StringEncryptConverter.stringEncryptor::encrypt)
                       .orElse("");
    }

    @Override
    public String convertToEntityAttribute(String dbString) {

        return Optional.ofNullable(dbString)
                       .filter(s -> !s.isEmpty())
                       .map(StringEncryptConverter.stringEncryptor::decrypt)
                       .orElse("");
    }
}
```

Entity에 Converter를 적용합니다.
```java
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String userId;

    //자동으로 DB 암복호화가 가능
    @Convert(converter = StringEncryptConverter.class)
    private String password;
}
```

---
>Scheduling & Async 설정

Spring에 `@Scheduled`를 사용한 배치 작업이나 `@Async`를 활용한 비동기 API 호출 작업을 위해서는 
`@Configuration` 클래스에 `@EnableScheduling` 과 `@EnableAsync` 를 추가해주면 별도의 설정 없이 사용 가능하며
각각의 TaskScheduler를 설정하거나 Error Handler를 설정하기 위해서는 다음과 같이 Configure를 통해 설정을 재정의 합니다.

- `SchedulingConfig`
```java
@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

    @Value("${schedule.threadPool.size:30}")
    private int poolSize;

    @Autowired
    @Qualifier("scheduledErrorHandler")
    private ErrorHandler errorHandler;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix("Task-Scheduler-");
        scheduler.setErrorHandler(errorHandler);
        scheduler.initialize();

        taskRegistrar.setTaskScheduler(scheduler);
    }
}
```
- `AsyncConfig`
```java
@Configuration
@EnableAsync
public class AsyncConfig extends AsyncConfigurerSupport {

    @Value("${async.threadPool.size:20}")
    private int poolSize;

    @Value("${async.threadPool.max:100}")
    private int maxPoolSize;

    @Value("${async.threadPool.keepAliveSeconds:60}")
    private int keepAliveSeconds;

    @Value("${async.threadPool.queueCapacity:1000}")
    private int queueCapacity;

    @Override
    public Executor getAsyncExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }
}
```

`@Scheduled`나  `@Async`가 설정된 메소드들은 에러가 발생할 경우 사후 처리가 어렵기 때문에 위 예시에서 보이는것과 같이
별도의 ExceptionHandler를 재작성하여 Fallback 처리를 하거나 Webhook 등을 발송 하여 에러를 처리 하는 것이 좋습니다.

---
>i18n과 에러메시지 처리 

Spring WebFlux의 LocaleResolver는 `AcceptHeaderLocaleContextResolver`와 `FixedLocaleContextResolver`를 통해 
Accept-Language 와 TimeZone 에 기반한 Locale 을 인지합니다.
따라서 QueryParam 과 Accept-Language 를 동시에 지원하기 위해 다음과 같이 `LocaleContextResolver`를 작성하여 
`DelegatingWebFluxConfiguration` 를 통해 LocaleResolver를 등록해 줘야 합니다.

- `LocaleResolver`
```java
public class LocaleResolver implements LocaleContextResolver {

    @Override
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {

        String language = exchange.getRequest().getQueryParams().getFirst("lang") != null
                        ? exchange.getRequest().getQueryParams().getFirst("lang")
                          : exchange.getRequest().getHeaders().getFirst(HttpHeaders.ACCEPT_LANGUAGE);

        Locale targetLocale = language != null && !language.isEmpty()
                              ? Locale.forLanguageTag(language.replaceAll("_", "-"))
                              : Locale.getDefault();

        return new SimpleLocaleContext(targetLocale);
    }

    @Override
    public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {

    }
}
```
- `LocaleConfig`
```java
@Configuration
public class LocaleConfig extends DelegatingWebFluxConfiguration {

    @Override
    protected LocaleContextResolver createLocaleContextResolver() {
        return new LocaleResolver();
    }
}
```

Spring WebFlux에서 에러 처리는 `ResponseStatusException` 을 REST API에서 발생시켜 주면 `DefaultErrorAttributes` 를 통해 JSON으로 반환되지만
i18n을 적용하기 위해서는 별도의 Exception 정의하고 `@RestControllerAdvice` 또는 `@ControllerAdvice` 통해서 Response 메시지를 재정의 해주는 것이 좋습니다.

어플리케이션에서 사용할 기본 Exception인  `AppBaseException` 과 에러코드 정의를 위한 `ErrorCode` 를 정의하고
Response 구조체인 `AppErrorResponse` 반환하는 `@RestControllerAdvice` 를 아래와 같이 작성하여  
어플리케이션에서 발생한 에러에 대한 i18n을 적용합니다.

- `GlobalAdvice`
```java
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalAdvice {

    private final MessageSource messageSource;

    @ExceptionHandler({AppBaseException.class})
    public ResponseEntity<AppErrorResponse> handleAppBaseException(AppBaseException ex, Locale locale, ServerWebExchange exchange) {

        if(ex.getCause() != null) {
            log.error(ex.getLocalizedMessage(), ex);
        }

        HttpStatus status = ex.getStatus() != null
                            ? ex.getStatus()
                            : HttpStatus.BAD_REQUEST;

        String errorMessage = ex.getErrorCode() != null
                              ? messageSource.getMessage(ex.getErrorCode().getValue(), ex.getArgs(), locale)
                              : null;

        return new ResponseEntity<>(
            AppErrorResponse.builder()
                            .timestamp(System.currentTimeMillis())
                            .path(exchange.getRequest().getPath().value())
                            .status(status.value())
                            .error(status.getReasonPhrase())
                            .message(errorMessage != null ? errorMessage : ex.getLocalizedMessage())
                            .requestId(exchange.getRequest().getId())
                            .build()
            , status
        );
    }
}
```

`MessageSource`를 위한 i18n message들은 `application.yaml`에 다음과 같이 정의 하고 해당 디렉토리 (classpath:/resource/i18n) 아래에 지원하는 Locale 별로 파일을 생성합니다.
(messages.properties, messages_en_US.properties, messages_ko_KR.properties 등)

- `application.yaml`
```yaml
spring:
    messages:
        basename: i18n/messages
        encoding: UTF-8
        use-code-as-default-message: true
```

어플리케이션 내부에서 사용하는 메시지 또한 `MessageSource`와 `Locale`을 통해 i18n을 적용 할 수 있습니다.

---
>Cache with Redis

Spring WebFlux에 Redis를 적용하기 위한 방식은 기본적으로 Spring MVC 방식과 동일하며,
`appliction.yaml`을 통해 Redis, Lettuce, Cache 관련 정보를 설정 해주면 바로 사용할 수 있습니다.
Reactive Template들을 추가로 사용 가능하게 하기 위해서 Reactive 관련 Bean을 추가로 설정 해 줍니다.

- `application.yaml`
```yaml
spring:
  redis:
    host: localhost
    port: 16379
    password: ENC(RE0TFEbdNyRMb+IwbeLLvrhKBAjKmStj)
    lettuce:
      pool:
        min-idle: 2
        max-idle: 5
        max-active: 10
  cache:
    type: redis
    redis:
      cache-null-values: false
      time-to-live: 60000
```
Spring Boot을 통해 Lettuce Connection Pool을 사용하기 위해서는 Apache Common Pool을 Maven에 추가해 줘야 합니다.

- `pom.xml`
```xml
<!--Common Pool -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>2.8.0</version>
</dependency>
```

`@Cacheable` 어노테이션을 통한 Cache를 사용하기 위해서는 다음과 같이 `@EnableCaching`을 설정합니다.

- `CacheConfig`
```java
@Configuration
@EnableCaching
public class CacheConfig {
}
```

Redis 설정 및 Template 재정의를 위해 다음과 같이 `@Configuration`을 추가해 줍니다.

- `RedisConfig`

```java
@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {

        RedisSerializationContext<String, Object> serializationContext =
            RedisSerializationContext.<String, Object>newSerializationContext(new StringRedisSerializer())
                                     .hashKey(new StringRedisSerializer())
                                     .hashValue(new GenericJackson2JsonRedisSerializer())
                                     .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}
```

** 현재 버전의 Spring WebFlux는 `@Cacheable` 어노테이션을 통한 `Mono` 또는 `Flux`의 캐싱을 지원하지 않습니다.
`Mono` 또는 `Flux`에 대한 캐싱을 적용하기 위해서는 reactor-extra에서 지원하는 CacheMono 등을 통해 직접 구현해야 합니다.

- `pom.xml`
```xml
<!-- Reactor Extra for Reactive Cache -->
<dependency>
    <groupId>io.projectreactor.addons</groupId>
    <artifactId>reactor-extra</artifactId>
</dependency>
```
캐시 구현은 아래와 같이 Util 클래스를 설정하고 API에서 활용하는 것이 편합니다.

- Util Class Method (`RxUtil.class` 참고)
```java
public static <T> Mono<T> cacheMono(CacheManager cacheManager, String cacheName, String key, Mono<T> retriever, Class<T> klass) {
    return
        CacheMono
            .lookup(
                k -> RxUtil.elasticMono(() -> Mono.justOrEmpty(cacheManager.getCache(cacheName).get(k, klass)).map(Signal::next) )
                , key
            )
            .onCacheMissResume(retriever)
            .andWriteWith((k, sig) ->
                RxUtil.elasticMono(() -> Optional.ofNullable(sig.get()).map(Mono::just).orElseGet(Mono::empty)
                                                 .doOnNext(o -> cacheManager.getCache(cacheName).put(k, o))
                                                 .then()
                )
            )
        ;
}
```

- WebFlux Cache Usage 
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class UserSimpleService implements UserService {

    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    @Qualifier("jdbcScheduler")
    private final Scheduler jdbcScheduler;

    @Transactional(readOnly = true)
    public Mono<User> findById(@NonNull String userId) {

        return
            RxUtil.cacheMono(
                cacheManager
                , "user"
                , userId
                , Mono.defer(() -> 
                        userRepository.findById(userId)
                                      .map(Mono::just)
                                      .orElseGet(Mono::empty)
                      )
                      .subscribeOn(jdbcScheduler)
                , User.class
            )
            ;
    }
}
```

---
>WebClient 설정

Spring WebFlux에서 제공하는 HTTP/1.1 기반의 Non-Blocking, Reactive Http Client로 `WebClient`를 지원하며
기존 Spring MVC에 `RestTemplate`을 대체하여 사용할 수 있습니다 (RestTemplate은 향후 Deprecated될 예정입니다.)

`WebClient` 를 사용하기 위해 `@Bean`으로 등록하고 아래와 같이 기본값들을 변경합니다.

- `WebClientConfig`
```java
@Configuration
@Slf4j
public class WebClientConfig {

    @Bean
    public WebClient webClient() {

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                                                                  .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024*1024*50))  //Body Contents 용량 증가
                                                                  .build();
        exchangeStrategies
            .messageWriters().stream()
            .filter(LoggingCodecSupport.class::isInstance)
            .forEach(writer -> ((LoggingCodecSupport)writer).setEnableLoggingRequestDetails(true));     //Logging 설정

        return WebClient.builder()
                        .clientConnector(
                            new ReactorClientHttpConnector(     // HttpClient 옵션 수정
                                HttpClient
                                    .create()
                                    .secure(
                                        ThrowingConsumer.unchecked(
                                            sslContextSpec -> sslContextSpec.sslContext(
                                                SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build()    //비인증 SSL 허가
                                            )
                                        )
                                    )
                                    .tcpConfiguration(
                                        client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120_000)                      //Connection TimeOut
                                                        .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(180))     //Read TimeOut
                                                                                   .addHandlerLast(new WriteTimeoutHandler(180))    //Write TimeOut
                                                        )
                                    )
                            )
                        )
                        .exchangeStrategies(exchangeStrategies)
                        .filter(ExchangeFilterFunction.ofRequestProcessor(      //Request Header Logging
                            clientRequest -> {
                                log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
                                clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.debug("{} : {}", name, value)));
                                return Mono.just(clientRequest);
                            }
                        ))
                        .filter(ExchangeFilterFunction.ofResponseProcessor(     //Response Header Logging
                            clientResponse -> {
                                clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> log.debug("{} : {}", name, value)));
                                return Mono.just(clientResponse);
                            }
                        ))
                        .defaultHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.3")
                        .build();
    }
}
```

사용법은 위에서 설정한 Bean을 mutate() 메소드를 통해 옵션을 Override한 새로운 client를 생성해서 이용합니다.

- Sample Usage
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class SampleRestService implements SampleService {

    @Value("${restKey}")
    private String restKey;

    private final WebClient webClient;

    public Mono<SampleResponse> search(SampleParam param) {

        return
            webClient.mutate()
                     .baseUrl("https://api.sample.com")
                     .build()
                     .get()
                     .uri("/search/something?query={QUERY}&sort={SORT}&page={PAGE}&size={SIZE}&target={TARGET}"
                         , param.getQuery()
                         , param.getSort() 
                         , param.getPage() 
                         , param.getSize() 
                         , param.getTarget() 
                     )
                     .accept(MediaType.APPLICATION_JSON)
                     .header(HttpHeaders.AUTHORIZATION, restKey)
                     .retrieve()
                     .onStatus(status -> status.is4xxClientError() || status.is5xxServerError()
                         , clientResponse -> clientResponse.bodyToMono(String.class).map(body -> new AppBaseException(body)))
                     .bodyToMono(SampleResponse.class)
                     .log()
            ;
    }
}
```

---
>Spring Security with JWT

Spring WebFlux + Spring Security + JWT를 조합한 가장 심플한 구성의 예시입니다.

기본 전략은 다음과 같습니다.

- JWT Token은 보안상 가장 최소한의 정보만 담습니다.
- JWT Token 자체에 기본 보안이 되어 있으므로 Token Validation 시 User 정보를 조회하지 않습니다.
- User 정보를 추가로 검증하려면 `AuthenticationJwtManager`에 검증 로직을 추가합니다.
- Token이 유효한지 자체는 Token 자체 만으로 검증합니다.
- 유효 Token 리스트를 별도로 저장하려면 `BearerSecurityContextRepository`에 검증 로직을 추가합니다.
- User의 가입 / 로그인 과 Token의 생성 / 재발급만 예시로 작성하였습니다.
- JWT 토큰은 auth0 라이브러리를 활용합니다.

Spring Security를 적용하기 위해 `pom.xml`에 관련 라이브러리를 추가해 줍니다.

- `pom.xml`
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>com.auth0</groupId>
        <artifactId>java-jwt</artifactId>
        <version>3.10.0</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>

</dependencies>
```
`@Configuration`을 통해 `SecurityWebFilterChain`과 `PasswordEncoder`를 설정해 줍니다.

- `SecurityConfig`
```java
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Autowired
    private AuthenticationJwtManager authenticationManager;

    @Autowired
    private BearerSecurityContextRepository securityContextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return
            http
                .exceptionHandling()
                    .authenticationEntryPoint((exchange, e) -> Mono.fromCallable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)).then())
                    .accessDeniedHandler((exchange, denied) -> Mono.fromCallable(() -> exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)).then())
                .and()
                .httpBasic().disable()
                .formLogin().disable()
                .logout().disable()
                .cors().disable()
                .csrf().disable()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                    .pathMatchers("/api/ping/**").permitAll()
                    .pathMatchers("/api/test/**").permitAll()
                    .pathMatchers("/api/auth/sign*").permitAll()
                    .pathMatchers(HttpMethod.OPTIONS).permitAll()
                    .pathMatchers("/api/**").authenticated()
                .anyExchange().permitAll()
                .and()
                .build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```

JWT 토큰 인증과 Method Security 적용을 위해 `ReactiveAuthenticationManager`와 `ServerSecurityContextRepository`을 사용자 지정하여 적용하였습니다.

각 구현체는 아래와 같습니다.

- `ReactiveAuthenticationManager`
```java
@Component
@RequiredArgsConstructor
public class AuthenticationJwtManager implements ReactiveAuthenticationManager {

    private final TokenService tokenService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        return
            Mono.just(authentication)
                .map(auth -> auth.getCredentials().toString())
                .flatMap(token -> Mono.just(tokenService.verifyToken(token)))       // JWT Token 검증
                .flatMap(decodedJWT -> Mono.just(decodedJWT)                        // 필요시 User 추가 검증 가능
                                           .map(jwt -> jwt.getClaim("scopes").asList(String.class).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()))
                                           .map(authorities ->
                                                new UsernamePasswordAuthenticationToken(
                                                    User.builder()
                                                        .username(decodedJWT.getSubject())
                                                        .password("Noop")
                                                        .authorities(authorities)
                                                        .build()
                                                    , null
                                                    , authorities
                                                    )
                                            )
                )
                .onErrorResume(throwable -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, throwable.getLocalizedMessage())))
                .flatMap(Mono::just)
            ;
    }
}
```

- `ServerSecurityContextRepository`
```java
@Component
@RequiredArgsConstructor
public class BearerSecurityContextRepository implements ServerSecurityContextRepository {

    private final ReactiveAuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {

        return
            RxUtil.elasticMono(() -> Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                                             .filter(s -> s.startsWith("Bearer "))
                                             .map(s -> s.substring(7))
                                             .map(Mono::just)
                                             .orElseGet(Mono::empty)
                  )
                  .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not found token, Please signIn again.")))
                  .map(token -> new UsernamePasswordAuthenticationToken(token, token))      //필요시 Token 추가 검증
                  .flatMap(authenticationManager::authenticate)
                  .map(SecurityContextImpl::new)
                  ;
    }
}
```

MethodSecurity는 다음과 같이 사용할 수 있으며, SecurityContext에서 User정보를 가져오기 위해서는 `@AuthenticationPrincipal` 을 통해 파라미터로 받는게 좋습니다.
(Spring WebFlux의 Context는 [여기](https://projectreactor.io/docs/core/release/reference/#context) 를 참고하면 좋습니다.)

- Usage
```java
@GetMapping("/refresh")
@PreAuthorize("hasAuthority('refresh')")
@ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, paramType = "header", dataTypeClass = String.class, example = "Bearer ...")
public Mono<TokenInfo> refresh(@ApiIgnore @AuthenticationPrincipal UserDetails userInfo) {

    return
        Mono.justOrEmpty(userInfo)
            .map(UserDetails::getUsername)
            .flatMap(userService::findById)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not found token, Please signin again.")))
            .flatMap(user -> RxUtil.elasticMono(() -> Mono.just(tokenService.createToken(user))))
        ;
}
```

