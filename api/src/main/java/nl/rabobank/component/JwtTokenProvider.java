package nl.rabobank.component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

  private final JWSSigner signer;

  public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret) throws KeyLengthException {
    signer = new MACSigner(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(Authentication auth) {
    var now = new Date();
    var exp = new Date(now.getTime() + 3600_000L);
    SignedJWT jwt = new SignedJWT(
      new JWSHeader(JWSAlgorithm.HS256),
      new JWTClaimsSet.Builder()
        .subject(auth.getName())
        .issueTime(now)
        .expirationTime(exp)
        .claim("roles", auth.getAuthorities().stream()
          .map(Object::toString).collect(Collectors.toList()))
        .build()
    );
    try {
      jwt.sign(signer);
    } catch (JOSEException e) {
      throw new RuntimeException("Failed to sign JWT token", e);
    }
    return jwt.serialize();
  }
}
