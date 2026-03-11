package moon.odyssey.webflux.application.auth.service

import com.auth0.jwt.interfaces.DecodedJWT
import moon.odyssey.webflux.application.auth.entity.User
import moon.odyssey.webflux.application.auth.model.TokenInfo

interface TokenService {
    fun createToken(user: User): TokenInfo
    fun verifyToken(token: String): DecodedJWT
}
