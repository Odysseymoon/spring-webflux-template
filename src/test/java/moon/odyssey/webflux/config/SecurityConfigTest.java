package moon.odyssey.webflux.config;

import org.assertj.core.api.Assertions;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SecurityConfigTest {

    private static String orgPassword = "testPassword";

    private static String encPassword = "";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void _0_testInit() {

        assertThat(passwordEncoder).isNotNull();

    }

    @Test
    public void _1_testEncrypt() {
        encPassword = passwordEncoder.encode(orgPassword);

        log.info("##### encrypted string : {}", encPassword);

        Assertions.assertThat(passwordEncoder.matches(orgPassword, encPassword))
                  .isTrue();
    }

}