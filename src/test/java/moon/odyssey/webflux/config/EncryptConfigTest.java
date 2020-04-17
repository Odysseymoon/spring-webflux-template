package moon.odyssey.webflux.config;

import org.jasypt.encryption.StringEncryptor;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class EncryptConfigTest {

    private static String ORIGINAL_STRING = "EncryptConfigTest";

    private static String ENCRYPTED_STRING = "";

    @Autowired
    private StringEncryptor jasyptStringEncryptor;

    @Test
    public void _0_testInit() {

        assertThat(jasyptStringEncryptor).isNotNull();

    }

    @Test
    public void _1_testEncrypt() {
        ENCRYPTED_STRING = jasyptStringEncryptor.encrypt(ORIGINAL_STRING);

        log.info("##### encrypted string : {}", ENCRYPTED_STRING);
    }

    @Test
    public void _2_testDecrypt() {

        String decryptedString = jasyptStringEncryptor.decrypt(ENCRYPTED_STRING);

        log.info("##### decrypted string : {}", decryptedString);

        assertThat(decryptedString).isEqualTo(ORIGINAL_STRING);

    }

}