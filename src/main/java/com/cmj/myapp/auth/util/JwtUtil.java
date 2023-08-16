package com.cmj.myapp.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cmj.myapp.auth.AuthProfile;

import java.util.Date;

//  JWT(JSON Web Token)을 생성
public class JwtUtil {
    // 상수 정의
    public String sign = "your-secret";
    public final int TOKEN_TIMEOUT = 1000 * 60 * 60 * 24 * 7;

    public String createToken(long id, String email, String nickname) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + TOKEN_TIMEOUT);

        Algorithm algorithm = Algorithm.HMAC256(sign); // 토큰서명생성

        return JWT.create().withSubject(String.valueOf(id)).withClaim("email", email)
                .withClaim("nickname", nickname).withIssuedAt(now).withExpiresAt(exp)
                .sign(algorithm);
        /*
        JWT에 id, email, nickname, 토큰발급시간, 토큰만료시간을 생성하고
        sing 서명해서 생성된 JWT문자열을 반환함.
        */
    }

    public AuthProfile validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(sign);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build(); // 검증객체

        try {
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            Long id = Long.valueOf(decodedJWT.getSubject());
            String email = decodedJWT.getClaim("email").asString();
            String nickname = decodedJWT.getClaim("nickname").asString();

            return  AuthProfile.builder().id(id).email(email).nickname(nickname).build();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
