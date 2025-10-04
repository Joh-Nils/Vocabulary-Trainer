package org.johnils.vokabeln.db;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.johnils.vokabeln.Main;
import org.johnils.vokabeln.auth.AuthController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/db")
public class DBAPI {

    @GetMapping("/languages")
    public String[] languages(HttpServletRequest request) {
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return null;
        }

        return getLanguages(AuthController.loggedIn.get(sessionCookie.getValue()));
    }
    private String[] getLanguages(String user) {
        File root = new File(UserController.users.get(user).root());
        File[] langs = root.listFiles();
        List<String> ret = new ArrayList<>();

        assert langs != null;
        for (File lang: langs) {
            if (lang.getName().equals("user.txt")) continue;

            ret.add(lang.getName().substring(0,lang.getName().lastIndexOf(".")));
        }

        return ret.toArray(new String[0]);
    }

    @PostMapping("/addLanguage")
    public ResponseEntity<String> addLanguage(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return ResponseEntity.status(401).body("not ok");
        }

        File root = new File(UserController.users.get(AuthController.loggedIn.get(sessionCookie.getValue())).root());
        File lang = new File(root.getAbsolutePath() + "/" + name + ".lang");

        if (lang.exists()) {
            return ResponseEntity.status(401).body("not ok");
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(lang));

            bw.flush();
            bw.close();
        } catch (IOException e) {
            return ResponseEntity.status(401).body("not ok");
        }

        return ResponseEntity.ok("ok");
    }
    @PostMapping("/deleteLanguage")
    public ResponseEntity<String> deleteLanguage(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return ResponseEntity.status(401).body("not authorized");
        }

        File root = new File(UserController.users.get(AuthController.loggedIn.get(sessionCookie.getValue())).root());
        File lang = new File(root.getAbsolutePath() + "/" + name + ".lang");

        if (!lang.exists()) {
            return ResponseEntity.status(401).body("Language does not exist");
        }

        if(!lang.delete()) {
            return ResponseEntity.status(401).body("Error while deleting");
        }


        return ResponseEntity.ok("ok");
    }

    @GetMapping("/vocabulary")
    public Vocab[] vocabulary(HttpServletRequest request, @RequestParam String language) {
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return null;
        }


        return getVocab(AuthController.loggedIn.get(sessionCookie.getValue()), language);
    }
    private Vocab[] getVocab(String user, String lang) {
        File langFile = new File(UserController.users.get(user).root() + "/" + lang + ".lang");
        if (!langFile.exists()) return null;
        List<Vocab> ret = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(langFile));

            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split("=");
                ret.add(new Vocab(args[0],args[1]));
            }

            br.close();
        } catch (IOException e) {
            return null;
        }


        return ret.toArray(new Vocab[0]);
    }

    @PostMapping("/addVocabulary")
    public ResponseEntity<String> addVocabulary(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String language = body.get("language");
        String word = body.get("word");
        String translation = body.get("translation");
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return ResponseEntity.status(401).body("not authorized");
        }

        File root = new File(UserController.users.get(AuthController.loggedIn.get(sessionCookie.getValue())).root());
        File lang = new File(root.getAbsolutePath() + "/" + language + ".lang");

        if (!lang.exists()) {
            return ResponseEntity.status(401).body("This language does not exist");
        }


        try {
            List<String> lines = Files.readAllLines(lang.toPath());
            if (lines.contains(word + "=" + translation)) return ResponseEntity.status(401).body("Vocab already exists");
        } catch (IOException e) {
            return ResponseEntity.status(401).body("Error while writing to disk");
        }

        try {
            FileWriter fw = new FileWriter(lang,true);

            fw.write( word + "=" + translation + "\n");

            fw.flush();
            fw.close();
        } catch (IOException e) {
            return ResponseEntity.status(401).body("Error while writing to disk");
        }

        return ResponseEntity.ok("ok");
    }

    @PostMapping("/deleteVocab")
    public ResponseEntity<String> deleteVocab(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String language = body.get("language");
        String word = body.get("word");
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return ResponseEntity.status(401).body("not authorized");
        }

        File root = new File(UserController.users.get(AuthController.loggedIn.get(sessionCookie.getValue())).root());
        File lang = new File(root.getAbsolutePath() + "/" + language + ".lang");

        if (!lang.exists()) {
            return ResponseEntity.status(401).body("This language does not exist");
        }


        try {
            List<String> lines = Files.readAllLines(lang.toPath());

            lines.removeIf(line -> line.startsWith(word + "="));

            // Write back to the same file
            Files.write(lang.toPath(), lines);
        } catch (IOException e) {
            return ResponseEntity.status(401).body("Error while writing to disk");
        }

        return ResponseEntity.ok("ok");
    }
    @PostMapping("/editVocab")
    public ResponseEntity<String> editVocab(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String language = body.get("language");
        String old = body.get("oldWord");
        String newWord = body.get("newWord");
        String newTranslation = body.get("newTranslation");
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return ResponseEntity.status(401).body("not authorized");
        }

        File root = new File(UserController.users.get(AuthController.loggedIn.get(sessionCookie.getValue())).root());
        File lang = new File(root.getAbsolutePath() + "/" + language + ".lang");

        if (!lang.exists()) {
            return ResponseEntity.status(401).body("This language does not exist");
        }


        try {
            List<String> lines = Files.readAllLines(lang.toPath());

            for (String line: lines) {
                if (line.startsWith(newWord + "=")) {
                    return ResponseEntity.status(401).body("Vocab with that word already exists");
                }

                if (line.startsWith(old + "=")) {
                    lines.set(lines.indexOf(line), newWord + "=" + newTranslation);
                    break;
                }
            }

            // Write back to the same file
            Files.write(lang.toPath(), lines);
        } catch (IOException e) {
            return ResponseEntity.status(401).body("Error while writing to disk");
        }

        return ResponseEntity.ok("ok");
    }


    //SESSION
    @PostMapping("/session/start")
    public ResponseEntity<String> startSession(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String language = body.get("language");
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return ResponseEntity.status(401).body("not authorized");
        }

        SessionController.startSession(UserController.users.get(AuthController.loggedIn.get(sessionCookie.getValue())),language);

        return ResponseEntity.ok("ok"); //TODO
    }
    @PostMapping("/session/end")
    public ResponseEntity<SessionData> endSession(HttpServletRequest request) {
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return ResponseEntity.status(401).body(null);
        }

        return ResponseEntity.ok(SessionController.endSession(UserController.users.get(AuthController.loggedIn.get(sessionCookie.getValue()))));
    }
    @PostMapping("/session/getVocab")
    public Vocab getSessionVocab(HttpServletRequest request) {
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return null;
        }

        return SessionController.getVocab(UserController.users.get(AuthController.loggedIn.get(sessionCookie.getValue())));
    }
    @PostMapping("/session/correct")
    public Correction correctVocab(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String translation = body.get("translation");
        Cookie sessionCookie = Main.getSession(request);
        if (sessionCookie == null || !AuthController.loggedIn.containsKey(sessionCookie.getValue())) {
            return null;
        }

        return SessionController.correct(UserController.users.get(AuthController.loggedIn.get(sessionCookie.getValue())), translation);
    }
}
