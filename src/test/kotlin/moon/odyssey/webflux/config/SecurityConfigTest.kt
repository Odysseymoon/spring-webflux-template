package moon.odyssey.webflux.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName::class)
class SecurityConfigTest {

    private val log = LoggerFactory.getLogger(SecurityConfigTest::class.java)

    companion object {
        private const val ORG_PASSWORD = "testPassword"
        private var ENC_PASSWORD = ""
    }

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun _0_testInit() {
        assertThat(passwordEncoder).isNotNull
    }

    @Test
    fun _1_testEncrypt() {
        ENC_PASSWORD = passwordEncoder.encode(ORG_PASSWORD)
        log.info("##### encrypted string : {}", ENC_PASSWORD)
        assertThat(passwordEncoder.matches(ORG_PASSWORD, ENC_PASSWORD)).isTrue
    }
}
