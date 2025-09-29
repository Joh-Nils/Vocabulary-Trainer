package org.johnils.vokabeln.core;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.johnils.vokabeln.auth.AuthController;
import org.johnils.vokabeln.IndexController;
import org.johnils.vokabeln.Main;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class DashboardController {
    private static final String index;


    static {
        try {
            index = new String(IndexController.class.getResource("/Websites/Dashboard/index.html").openStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, HttpServletResponse response) {
        Cookie session = Main.getSession(request);

        if (session != null && AuthController.loggedIn.containsKey(session.getValue())) {
            return index;
        }

        try {
            response.sendRedirect("/");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "";
    }
}
