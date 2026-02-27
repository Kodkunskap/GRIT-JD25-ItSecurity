package security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*

Exempel payload på JWT-token - kan användas i t.ex. Postman
Detta skickar du som en "Authentication" Bearer.

{
  "iss": "https://issuer.example",
  "aud": "my-api",
  "sub": "username"
}


 */

import java.io.IOException;
/*
    WebFilter används för att kontrollera requests innan request skickas vidare till servlets,
    utmärkt att använda för att kontrollera vem användaren är
 */
@WebFilter(urlPatterns = "/api/*")
public class JwtAuthFilter implements Filter {

    private JwtVerifier verifier;

    @Override
    public void init(FilterConfig filterConfig) {
        // I verkligheten: läs från env/konfig/secret store, inte hårdkodat
        String secret = "CHANGE_ME_TO_A_LONG_RANDOM_SECRET_AT_LEAST_32_BYTES!"; // Att fixa - byt lösenordet här
        String issuer = "https://issuer.example";
        String audience = "my-api";
        this.verifier = new JwtVerifier(secret, issuer, audience);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            unauthorized(response, "Missing Bearer token");
            return;
        }

        String token = auth.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            unauthorized(response, "Empty token");
            return;
        }

        try {
            Claims claims = verifier.verify(token);

            request.setAttribute("jwt.claims", claims);
            request.setAttribute("jwt.subject", claims.getSubject());

            chain.doFilter(request, response);
        } catch (JwtException e) {
            unauthorized(response, "Invalid token");
        }
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}