package moon.odyssey.webflux.application.advice;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import lombok.extern.slf4j.Slf4j;
import moon.odyssey.webflux.utils.exception.AppErrorResponse;

@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GlobalAdviceTest {

    @Autowired
    private WebTestClient testClient;

    @Test
    public void _1_testError_Should_Return_AppErrorResponse() {
        testClient
            .get()
            .uri("/api/test/error")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(AppErrorResponse.class)
            .consumeWith(result -> log.info(result.toString()))
            ;
    }

    @Test
    public void _2_testErrorWithQueryParam_Should_Return_AppErrorResponse() {
        testClient
            .get()
            .uri("/api/test/error?lang=en-US")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(AppErrorResponse.class)
            .consumeWith(result -> log.info(result.toString()))
        ;
    }

    @Test
    public void _3_testErrorWithHeader_Should_Return_AppErrorResponse() {
        testClient
            .get()
            .uri("/api/test/error")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR")
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(AppErrorResponse.class)
            .consumeWith(result -> log.info(result.toString()))
        ;
    }

}