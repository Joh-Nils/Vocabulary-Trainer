package org.johnils.vokabeln.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.johnils.vokabeln.Main;
import org.johnils.vokabeln.db.UserController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController()
@RequestMapping("/auth")
public class AuthController {
    public static final Map<String,String> loggedIn = new ConcurrentHashMap<>();


    @PostMapping("/login")
    public ResponseEntity<String> login(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null) {
            return ResponseEntity.status(401).body("not ok");
        }

        if (UserController.authorize(username,password)) {
            loggedIn.put(sessionCookie.getValue(),username);
            return ResponseEntity.ok("ok");
        } else {
            return ResponseEntity.status(401).body("not ok");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null) {
            return ResponseEntity.status(401).body("not ok");
        }

        if (UserController.createUser(username,password)) {
            loggedIn.put(sessionCookie.getValue(),username);
            return ResponseEntity.ok("ok");
        } else {
            return ResponseEntity.status(401).body("not ok");
        }
    }
    @PostMapping("/delete")
    public ResponseEntity<String> delete(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String password = body.get("password");
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null) {
            return ResponseEntity.status(401).body("not ok");
        }
        if (!UserController.authorize(loggedIn.get(sessionCookie.getValue()), password)) {
            return ResponseEntity.status(401).body("not authorized");
        }

        UserController.deleteUser(loggedIn.get(sessionCookie.getValue()));
        loggedIn.remove(sessionCookie.getValue());
        return ResponseEntity.ok("ok");
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null) {
            return ResponseEntity.status(401).body("not ok");
        }

        loggedIn.remove(sessionCookie.getValue());
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/getName")
    public String name(HttpServletRequest request) {
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null) return "Profile";

        return loggedIn.get(sessionCookie.getValue());
    }
}
