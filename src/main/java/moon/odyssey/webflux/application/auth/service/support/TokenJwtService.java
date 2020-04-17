package moon.odyssey.webflux.application.auth.service.support;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moon.odyssey.webflux.application.auth.entity.User;
import moon.odyssey.webflux.application.auth.model.TokenInfo;
import moon.odyssey.webflux.application.auth.service.TokenService;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenJwtService implements TokenService {

    @Value("${auth.jwt.secretKey}")
    private String secretKey;

    @Value("${auth.jwt.issuer}")
    private String issuer;

    @Value("${auth.jwt.expireDays:30}")
    private Long expireDays;


    public TokenInfo createToken(@NonNull User user) {

        ZonedDateTime expires = LocalDateTime.now().atZone(ZoneId.systemDefault()).plusDays(expireDays);
        String accessToken = JWT.create()
                                .withSubject(user.getUserId())
                                .withIssuer(issuer)
                                .withClaim("scopes", user.getAuthorities().stream().map(scope -> scope.getAuthority()).collect(Collectors.toList()))
                                .withIssuedAt(new Date())
                                .withExpiresAt(Date.from(expires.toInstant()))
                                .sign(Algorithm.HMAC256(secretKey));
        return
            TokenInfo.builder()
                     .user(user.getUserId())
                     .type("Bearer")
                     .accessToken(accessToken)
                     .expiresAt(expires.toLocalDateTime())
                     .build()
            ;
    }

    public DecodedJWT verifyToken(@NonNull String token) {

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                                  .withIssuer(issuer)
                                  //.acceptExpiresAt(3600L * 24 * 1)
                                  .build();

        return verifier.verify(token);
    }
}
