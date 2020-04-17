package moon.odyssey.webflux.application.auth.service;

import org.assertj.core.api.Assertions;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import moon.odyssey.webflux.application.auth.entity.User;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void _0_init() {
        Assertions.assertThat(userService).isNotNull();
    }

    @Test
    public void _1_findById_should_return_User() {

        StepVerifier.create(userService.findById("testUser"))
                    .thenConsumeWhile(user -> {
                        log.info("##### {}", user);
                        Assertions.assertThat(user).isInstanceOf(User.class);
                        return true;
                    })
                    .verifyComplete();
    }

    @Test
    @Transactional
    @Rollback
    public void _2_addUser_should_return_NewUser() {

        StepVerifier.create(userService.addUser("testUser2", "testPassword2"))
                    .thenConsumeWhile(user -> {
                        log.info("##### {}", user);
                        Assertions.assertThat(user).isInstanceOf(User.class);
                        return true;
                    })
                    .verifyComplete();

    }

}