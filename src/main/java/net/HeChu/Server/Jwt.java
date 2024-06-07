package net.HeChu.Server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import net.HeChu.Common.UserInfo;

import java.time.Instant;
import java.util.Date;
import java.util.logging.Logger;

public class Jwt {
    private static final String SECRET = "secret";

    private static final Logger LOGGER = Logger.getLogger(Jwt.class.getName());

    public static boolean verify(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (Exception e) {
            LOGGER.warning("Error verifying token: " + e.getMessage());
            return false;
        }
    }

    public static String generate(UserInfo user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            String token = JWT.create()
                    .withClaim("id", user.getId())
                    .withIssuer("HeChuIM")
                    .withExpiresAt(Instant.now().plusSeconds(3600))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            LOGGER.warning("Error generating token: " + exception.getMessage());
            throw exception;
        }
    }
}
