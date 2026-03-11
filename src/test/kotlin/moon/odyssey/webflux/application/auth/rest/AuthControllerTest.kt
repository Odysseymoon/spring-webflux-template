package moon.odyssey.webflux.application.auth.rest

import moon.odyssey.webflux.application.auth.entity.Scope
import moon.odyssey.webflux.application.auth.entity.User
import moon.odyssey.webflux.application.auth.model.TokenInfo
import moon.odyssey.webflux.application.auth.model.UserParam
import moon.odyssey.webflux.application.auth.service.UserService
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.MethodName::class)
class AuthControllerTest {

    private val log = LoggerFactory.getLogger(AuthControllerTest::class.java)

    @MockBean
    private lateinit var userService: UserService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var testClient: WebTestClient

    @Test
    fun _1_signUp_Should_Return_OK() {
        val param = UserParam("testUser2", "testPassword2")

        Mockito.`when`(userService.findById(param.userId))
            .thenReturn(Mono.empty())

        Mockito.`when`(userService.addUser(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(
                Mono.just(
                    User(
                        param.userId,
                        passwordEncoder.encode(param.password),
                        mutableSetOf(Scope("refresh", "refresh authority "))
                    )
                )
            )

        testClient
            .post()
            .uri("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(param)
            .exchange()
            .expectStatus().isCreated
            .expectBody(Void::class.java)
            .consumeWith { log.info("##### OK") }

        Mockito.verify(userService, Mockito.times(1)).addUser(Mockito.anyString(), Mockito.anyString())
    }

    @Test
    fun _2_signUpDuplication_Should_Return_ERROR() {
        val param = UserParam("testUser", "testPassword")

        Mockito.`when`(userService.findById(param.userId))
            .thenReturn(
                Mono.just(
                    User(
                        param.userId,
                        passwordEncoder.encode(param.password),
                        mutableSetOf(Scope("refresh", "refresh authority "))
                    )
                )
            )

        Mockito.`when`(userService.addUser(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(
                Mono.just(
                    User(
                        param.userId,
                        passwordEncoder.encode(param.password),
                        mutableSetOf(Scope("refresh", "refresh authority "))
                    )
                )
            )

        testClient
            .post()
            .uri("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(param)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody(String::class.java)
            .consumeWith { info -> log.info("##### {}", info) }
    }

    @Test
    fun _3_signIn_Should_Return_Token() {
        val param = UserParam("testUser", "testPassword")

        Mockito.`when`(userService.findById(param.userId))
            .thenReturn(
                Mono.just(
                    User(
                        param.userId,
                        passwordEncoder.encode(param.password),
                        mutableSetOf(Scope("refresh", "refresh authority "))
                    )
                )
            )

        testClient
            .post()
            .uri("/api/auth/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(param)
            .exchange()
            .expectStatus().isOk
            .expectBody(TokenInfo::class.java)
            .consumeWith { info -> log.info("##### {}", info) }
    }

    @Test
    fun _4_signInNoFound_Should_Return_ERROR() {
        val param = UserParam("testUser2", "testPassword2")

        Mockito.`when`(userService.findById(param.userId))
            .thenReturn(Mono.empty())

        testClient
            .post()
            .uri("/api/auth/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(param)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(String::class.java)
            .consumeWith { info -> log.info("##### {}", info) }
    }

    @Test
    fun _5_refresh_Should_Return_Token() {
        val param = UserParam("testUser", "testPassword")

        Mockito.`when`(userService.findById(param.userId))
            .thenReturn(
                Mono.just(
                    User(
                        param.userId,
                        passwordEncoder.encode(param.password),
                        mutableSetOf(Scope("refresh", "refresh authority "))
                    )
                )
            )

        val accessToken = testClient
            .post()
            .uri("/api/auth/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(param)
            .exchange()
            .expectStatus().isOk
            .expectBody(TokenInfo::class.java)
            .returnResult()
            .responseBody
            ?.accessToken
            ?: throw IllegalStateException("Missing access token")

        testClient
            .get()
            .uri("/api/auth/refresh")
            .accept(MediaType.APPLICATION_JSON)
            .headers { headers -> headers.setBearerAuth(accessToken) }
            .exchange()
            .expectStatus().isOk
            .expectBody(TokenInfo::class.java)
            .consumeWith { info -> log.info("##### {}", info.responseBody) }
    }

    @Test
    fun _6_refreshWithoutToken_Should_Return_ERROR() {
        testClient
            .get()
            .uri("/api/auth/refresh")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody(String::class.java)
            .consumeWith { info -> log.info("##### {}", info) }
    }
}
