package org.johnils.vokabeln.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionController {
    private static final Map<User, Session> sessions = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LogManager.getLogger();


    public static void startSession(User user, String language) {
        sessions.put(user,load(user, language));
    }
    public static SessionData endSession(User user) {
        if (sessions.get(user)== null) return null;

        SessionData data = sessions.get(user).getData();

        sessions.remove(user);

        return data;
    }

    public static Vocab getVocab(User user) {
        return sessions.get(user).getVocab();
    }
    public static Correction correct(User user, String translation) {
        if (sessions.get(user) == null) return new Correction(false,"");
        boolean correct = sessions.get(user).correct(translation);
        return new Correction(correct, sessions.get(user).getLastVocab().translation());
    }


    private static Session load(User user, String language) {
        File file = new File(user.root() + "/" + language + ".lang");
        if (!file.exists()) return null;

        Session session = new Session(language);
        List<Vocab> vocabs = new ArrayList<>();


        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split("=");
                if (args.length!=2) continue;

                vocabs.add(new Vocab(args[0], args[1]));
            }

            br.close();
        } catch (IOException e) {
            LOGGER.warn("Couldn't read language '{}' for User '{}'",language, user.name());
        }

        session.vocabs = vocabs.toArray(new Vocab[0]);
        return session;
    }
}
