package com.herokuapp.projectideas.login;

import java.io.RandomAccessFile;
import java.util.Optional;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.documents.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @Autowired
    Database database;

    @Value("classpath:adjectives.txt")
    private Resource adjectivesFile;

    @Value("classpath:nouns.txt")
    private Resource nounsFile;

    public User getUserByEmail(String email) {
        Optional<User> user = database.findUserByEmail(email);
        if (!user.isPresent()) {
            return database.createUser(new User(generateUsername(), email));
        }
        return user.get();
    }

    private String generateUsername() {
        try {
            RandomAccessFile adjectives = new RandomAccessFile(adjectivesFile.getFile(), "r");
            RandomAccessFile nouns = new RandomAccessFile(nounsFile.getFile(), "r");
            long randomLocation = (long) (Math.random() * (adjectives.length()-1));
            adjectives.seek(randomLocation);
            randomLocation = (long) (Math.random() * (nouns.length()-1));
            nouns.seek(randomLocation);
            adjectives.readLine();
            nouns.readLine();

            String username = adjectives.readLine().replace("\n", "")+nouns.readLine().replace("\n", "");

            adjectives.close();
            nouns.close();
            return username;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}