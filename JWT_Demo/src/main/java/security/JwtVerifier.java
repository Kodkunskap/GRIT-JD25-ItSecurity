package security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class JwtVerifier {

    private final SecretKey key;
    private final String expectedIssuer;
    private final String expectedAudience;

    public JwtVerifier(String hmacSecret, String expectedIssuer, String expectedAudience) {
        Objects.requireNonNull(hmacSecret, "hmacSecret");
        // För HS256 behöver secret vara tillräckligt långt (minst 256 bit = 32 bytes).
        this.key = Keys.hmacShaKeyFor(hmacSecret.getBytes(StandardCharsets.UTF_8));
        this.expectedIssuer = expectedIssuer;
        this.expectedAudience = expectedAudience;
    }

    /**
     * Verifierar signatur + exp/nbf automatiskt, samt issuer/audience om angivet.
     * Returnerar Claims om allt är OK, annars kastas JwtException.
     */
    public Claims verify(String jwt) throws JwtException {

        var parser = null; // Att fixa (null) - skapa en JwtParser här - den behöver använda din hemliga nyckel (SecretKey key)

        Claims claims = null; // Att fixa (null) - använd din parser för att parsa jwt-tokenet här

        if (expectedIssuer != null && !expectedIssuer.equals(claims.getIssuer())) {
            throw new JwtException("Invalid issuer");
        }
        if (expectedAudience != null) {
            // aud kan vara sträng eller lista; JJWT mappar ofta till String eller Collection.
            Object aud = claims.get("aud");
            if (aud == null || !aud.toString().contains(expectedAudience)) {
                throw new JwtException("Invalid audience");
            }
        }

        return claims;
    }
}