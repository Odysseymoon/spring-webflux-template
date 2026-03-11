package moon.odyssey.webflux.application.auth.service

import moon.odyssey.webflux.application.auth.entity.User
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import reactor.test.StepVerifier

@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName::class)
class UserServiceTest {

    private val log = LoggerFactory.getLogger(UserServiceTest::class.java)

    @Autowired
    private lateinit var userService: UserService

    @Test
    fun _0_init() {
        Assertions.assertThat(userService).isNotNull
    }

    @Test
    fun _1_findById_should_return_User() {
        StepVerifier.create(userService.findById("testUser"))
            .thenConsumeWhile { user ->
                log.info("##### {}", user)
                Assertions.assertThat(user).isInstanceOf(User::class.java)
                true
            }
            .verifyComplete()
    }

    @Test
    @Transactional
    @Rollback
    fun _2_addUser_should_return_NewUser() {
        StepVerifier.create(userService.addUser("testUser2", "testPassword2"))
            .thenConsumeWhile { user ->
                log.info("##### {}", user)
                Assertions.assertThat(user).isInstanceOf(User::class.java)
                true
            }
            .verifyComplete()
    }
}
