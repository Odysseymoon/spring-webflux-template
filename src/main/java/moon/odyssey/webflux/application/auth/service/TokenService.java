package moon.odyssey.webflux.application.auth.service;

import com.auth0.jwt.interfaces.DecodedJWT;

import moon.odyssey.webflux.application.auth.entity.User;
import moon.odyssey.webflux.application.auth.model.TokenInfo;

public interface TokenService {

    TokenInfo createToken(User user);

    DecodedJWT verifyToken(String token);

}
