package moon.odyssey.webflux.config

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.jasypt.encryption.StringEncryptor
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EncryptConfig(
    @Value("\${encrypt.password:#\$%^&*OdyseeyMoon*&^%\$#}")
    private val password: String
) {

    @Bean("jasyptStringEncryptor")
    fun stringEncryptor(): StringEncryptor {
        val encryptor = PooledPBEStringEncryptor()
        val config = SimpleStringPBEConfig()
        config.password = password
        config.algorithm = "PBEWithSHA1AndDESede"
        config.keyObtentionIterations = 1000
        config.poolSize = 1
        config.provider = BouncyCastleProvider()
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator")
        config.stringOutputType = "base64"
        encryptor.setConfig(config)
        return encryptor
    }
}
