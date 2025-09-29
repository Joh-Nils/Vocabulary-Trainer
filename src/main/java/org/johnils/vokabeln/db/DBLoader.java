package org.johnils.vokabeln.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class DBLoader {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void load() {

        if (!UserController.root.exists()) {
            generate();
        }

        File[] users = UserController.root.listFiles();
        int successfulCount = 0;

        assert users != null;
        for (File user: users) {
            String name = null;
            String password = null;

            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(user.getAbsolutePath() + "/user.txt")));

                String line;
                while ((line = br.readLine()) != null) {
                    String[] args = line.split("=");
                    if (args.length!=2) continue;

                    String arg1 = args[1].trim();
                    if (arg1.startsWith("\"")) arg1 = arg1.substring(1);
                    if (arg1.endsWith("\"")) arg1 = arg1.substring(0,arg1.length() - 1);

                    switch (args[0]) {
                        case "name" -> {
                            name=arg1;
                        }

                        case "password" -> {
                            password=arg1;

                        }

                        default -> LOGGER.warn("Unexpected value '{}' in user.txt of User '{}'",args[0], user.getName());
                    }

                }

                br.close();
            } catch (IOException e) {
                LOGGER.error("Couldn't open user.txt for writing for User '{}'", user.getName());
                continue;
            }

            if (name == null || password == null) {
                LOGGER.warn("user.txt of User '{}' is missing values",user.getName());
                continue;
            }

            UserController.users.put(name,new User(name,password,user.getAbsolutePath()));
            successfulCount++;
        }


        LOGGER.info("Loaded {}/{} Users",successfulCount, users.length);
    }

    private static void generate() {
        UserController.root.mkdir();
    }
}
