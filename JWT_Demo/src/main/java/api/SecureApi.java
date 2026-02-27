package api;

import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "SecureApi", urlPatterns = "/api/hello")
public class SecureApi extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Claims claims = (Claims) req.getAttribute("jwt.claims");
        String subject = (String) req.getAttribute("jwt.subject");

        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write("{\"message\":\"Hello\",\"subject\":\"" + safe(subject) + "\",\"issuer\":\"" +
                safe(claims.getIssuer()) + "\"}");
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}