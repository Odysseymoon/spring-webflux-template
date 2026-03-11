package moon.odyssey.webflux.config

import org.assertj.core.api.Assertions.assertThat
import org.jasypt.encryption.StringEncryptor
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName::class)
class EncryptConfigTest {

    private val log = LoggerFactory.getLogger(EncryptConfigTest::class.java)

    companion object {
        private const val ORIGINAL_STRING = "EncryptConfigTest"
        private var ENCRYPTED_STRING = ""
    }

    @Autowired
    private lateinit var jasyptStringEncryptor: StringEncryptor

    @Test
    fun _0_testInit() {
        assertThat(jasyptStringEncryptor).isNotNull
    }

    @Test
    fun _1_testEncrypt() {
        ENCRYPTED_STRING = jasyptStringEncryptor.encrypt(ORIGINAL_STRING)
        log.info("##### encrypted string : {}", ENCRYPTED_STRING)
    }

    @Test
    fun _2_testDecrypt() {
        val decryptedString = jasyptStringEncryptor.decrypt(ENCRYPTED_STRING)
        log.info("##### decrypted string : {}", decryptedString)
        assertThat(decryptedString).isEqualTo(ORIGINAL_STRING)
    }
}
