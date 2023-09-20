package org.nrg.xnatx.plugins.transporter.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

//@Service
//@Slf4j
//public class DefaultTransporterAppAuthService implements TransporterAppAuthService{
//
//
//    private final String SECRET_KEY = "mySecretKey"; // In a real-world scenario, store this securely.
//    private static final long EXPIRATION_TIME = 3_600_000;
//
//    @Override
//    public String generateToken(String transportAppName, String secretKey) {
//        Algorithm algorithm = Algorithm.HMAC256(secretKey);
//        return JWT.create()
//                .withSubject(transportAppName)
//                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .sign(algorithm);
//    }
//
//    @Override
//    public DecodedJWT decodeToken(String token, String secretKey) throws JWTVerificationException {
//        Algorithm algorithm = Algorithm.HMAC256(secretKey);
//        JWTVerifier verifier = JWT.require(algorithm).build();
//        return verifier.verify(token);
//    }
//
//    @Override
//    public String extractTransportAppName(String token, String secretKey) throws JWTVerificationException {
//        return decodeToken(token, secretKey).getSubject();
//    }
//
//    @Override
//    public boolean validateToken(String token, String secretKey) {
//        try {
//            decodeToken(token, secretKey);
//          return true;
//        } catch (JWTVerificationException e) {
//            return false;
//        }
//    }
//
//}
