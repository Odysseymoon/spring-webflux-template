package moon.odyssey.webflux.application.auth.service.support

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import moon.odyssey.webflux.application.auth.entity.User
import moon.odyssey.webflux.application.auth.model.TokenInfo
import moon.odyssey.webflux.application.auth.service.TokenService
import moon.odyssey.webflux.config.properties.AuthJwtProperties
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

@Service
class TokenJwtService(
    private val properties: AuthJwtProperties
) : TokenService {

    override fun createToken(user: User): TokenInfo {
        val expires: ZonedDateTime = LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .plusDays(properties.expireDays)

        val accessToken = JWT.create()
            .withSubject(user.userId)
            .withIssuer(properties.issuer)
            .withClaim("scopes", user.authorities.map { it.authority })
            .withIssuedAt(Date())
            .withExpiresAt(Date.from(expires.toInstant()))
            .sign(Algorithm.HMAC256(properties.secretKey))

        return TokenInfo(
            user = user.userId,
            type = "Bearer",
            accessToken = accessToken,
            expiresAt = expires.toLocalDateTime()
        )
    }

    override fun verifyToken(token: String): DecodedJWT {
        val verifier: JWTVerifier = JWT.require(Algorithm.HMAC256(properties.secretKey))
            .withIssuer(properties.issuer)
            .build()
        return verifier.verify(token)
    }
}
