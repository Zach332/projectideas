package com.herokuapp.projectideas.login;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.user.User;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @Autowired
    Database database;

    private File adjectivesFile;
    private File nounsFile;

    public LoginController(
        @Autowired Environment environment,
        @Value("classpath:adjectives.txt") Resource adjectives,
        @Value("classpath:nouns.txt") Resource nouns
    ) {
        try {
            if (environment.acceptsProfiles(Profiles.of("prod"))) {
                adjectivesFile =
                    File.createTempFile("projectideas-adjectives", ".tmp");
                adjectivesFile.deleteOnExit();
                copyStream(
                    adjectives.getInputStream(),
                    new FileOutputStream(adjectivesFile)
                );

                nounsFile = File.createTempFile("projectideas-nouns", ".tmp");
                nounsFile.deleteOnExit();
                copyStream(
                    nouns.getInputStream(),
                    new FileOutputStream(nounsFile)
                );
            } else {
                adjectivesFile = adjectives.getFile();
                nounsFile = nouns.getFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUserByEmail(String email) {
        Optional<User> user = database.getUserByEmail(email);
        if (!user.isPresent()) {
            database.createUser(new User(generateUsername(), email));
            return database.getUserByEmail(email).get();
        }
        return user.get();
    }

    private String generateUsername() {
        try {
            RandomAccessFile adjectives = new RandomAccessFile(
                adjectivesFile,
                "r"
            );
            RandomAccessFile nouns = new RandomAccessFile(nounsFile, "r");
            long randomLocation = (long) (
                Math.random() * (adjectives.length() - 1)
            );
            adjectives.seek(randomLocation);
            randomLocation = (long) (Math.random() * (nouns.length() - 1));
            nouns.seek(randomLocation);
            adjectives.readLine();
            nouns.readLine();

            String username =
                adjectives.readLine().replace("\n", "") +
                nouns.readLine().replace("\n", "");

            adjectives.close();
            nouns.close();
            return username;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void copyStream(InputStream in, OutputStream out)
        throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
