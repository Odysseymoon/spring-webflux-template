package moon.odyssey.webflux.application.auth.rest;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.HashSet;

import lombok.extern.slf4j.Slf4j;
import moon.odyssey.webflux.application.auth.entity.Scope;
import moon.odyssey.webflux.application.auth.entity.User;
import moon.odyssey.webflux.application.auth.model.TokenInfo;
import moon.odyssey.webflux.application.auth.model.UserParam;
import moon.odyssey.webflux.application.auth.service.UserService;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AuthControllerTest {

    @MockBean
    UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebTestClient testClient;

    @Test
    public void _1_signUp_Should_Return_OK() {

        UserParam param = new UserParam("testUser2", "testPassword2");

        Mockito.when(userService.findById(param.getUserId()))
               .thenReturn(Mono.empty());

        Mockito.when(userService.addUser(Mockito.anyString(), Mockito.anyString()))
               .thenReturn(Mono.just(new User(param.getUserId(), passwordEncoder.encode(param.getPassword()), new HashSet<>(Arrays.asList(new Scope("refresh", "refresh authority "))))));


        testClient
            .post()
            .uri("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(param)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Void.class)
            .consumeWith(aVoid -> log.info("##### OK"));

        Mockito.verify(userService, Mockito.times(1)).addUser(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void _2_signUpDuplication_Should_Return_ERROR() {

        UserParam param = new UserParam("testUser", "testPassword");

        Mockito.when(userService.findById(param.getUserId()))
               .thenReturn(Mono.just(new User(param.getUserId(), passwordEncoder.encode(param.getPassword()), new HashSet<>(Arrays.asList(new Scope("refresh", "refresh authority "))))));

        Mockito.when(userService.addUser(Mockito.anyString(), Mockito.anyString()))
               .thenReturn(Mono.just(new User(param.getUserId(), passwordEncoder.encode(param.getPassword()), new HashSet<>(Arrays.asList(new Scope("refresh", "refresh authority "))))));

        testClient
            .post()
            .uri("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(param)
            .exchange()
            .expectStatus().is4xxClientError()
            .expectBody(String.class)
            .consumeWith(info -> log.info("##### {}", info))
        ;

    }

    @Test
    public void _3_signIn_Should_Return_Token() {

        UserParam param = new UserParam("testUser", "testPassword");

        Mockito.when(userService.findById(param.getUserId()))
               .thenReturn(Mono.just(new User(param.getUserId(), passwordEncoder.encode(param.getPassword()), new HashSet<>(Arrays.asList(new Scope("refresh", "refresh authority "))))));

        testClient
            .post()
            .uri("/api/auth/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(param)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TokenInfo.class)
            .consumeWith(info -> log.info("##### {}", info));

    }

    @Test
    public void _4_signInNoFound_Should_Return_ERROR() {

        UserParam param = new UserParam("testUser2", "testPassword2");

        Mockito.when(userService.findById(param.getUserId()))
               .thenReturn(Mono.empty());

        testClient
            .post()
            .uri("/api/auth/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(param)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(String.class)
            .consumeWith(info -> log.info("##### {}", info))
        ;

    }

    @Test
    public void _5_refresh_Should_Return_Token() {

        UserParam param = new UserParam("testUser", "testPassword");

        Mockito.when(userService.findById(param.getUserId()))
               .thenReturn(Mono.just(new User(param.getUserId(), passwordEncoder.encode(param.getPassword()), new HashSet<>(Arrays.asList(new Scope("refresh", "refresh authority "))))));

        String accessToken = testClient
            .post()
            .uri("/api/auth/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(param)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TokenInfo.class)
            .returnResult()
            .getResponseBody()
            .getAccessToken();

        testClient
            .get()
            .uri("/api/auth/refresh")
            .accept(MediaType.APPLICATION_JSON)
            .headers(h -> h.setBearerAuth(accessToken))
            .exchange()
            .expectStatus().isOk()
            .expectBody(TokenInfo.class)
            .consumeWith(info -> log.info("##### {}", info.getResponseBody()))
        ;
    }

    @Test
    public void _6_refreshWithoutToken_Should_Return_ERROR() {

        testClient
            .get()
            .uri("/api/auth/refresh")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody(String.class)
            .consumeWith(info -> log.info("##### {}", info))
        ;
    }

}