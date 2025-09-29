package org.johnils.vokabeln;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.johnils.vokabeln.auth.AuthController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
public class IndexController {

    private static final String LoginFile;

    static {
        try {
            LoginFile = new String(IndexController.class.getResource("/Websites/Login/index.html").openStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final SecureRandom secureRandom = new SecureRandom(); // thread-safe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public static String generateToken(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    @GetMapping("/")
    public String index(HttpServletRequest request, HttpServletResponse response) {
        Cookie sessionCookie = Main.getSession(request);

        if (sessionCookie == null) {
            Cookie newCookie = new Cookie("session", generateToken(24));
            newCookie.setMaxAge(24 * 60 * 60); // 1 day
            newCookie.setHttpOnly(true);
            newCookie.setPath("/");
            response.addCookie(newCookie);

            return LoginFile;
        } else if (AuthController.loggedIn.containsKey(sessionCookie.getValue())){
            try {
                response.sendRedirect("/dashboard");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return "";
        }

        return LoginFile;
    }
}
