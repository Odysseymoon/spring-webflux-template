package moon.odyssey.webflux

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName::class)
class WebfluxApplicationTest {

    @Test
    fun _0_init() {
        Assertions.assertThat(this).isNotNull
    }
}
