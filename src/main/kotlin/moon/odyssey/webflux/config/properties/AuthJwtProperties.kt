package moon.odyssey.webflux.config.properties

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "auth.jwt")
@Validated
data class AuthJwtProperties(
    @field:NotBlank val secretKey: String,
    @field:NotBlank val issuer: String,
    val expireDays: Long = 30
)
