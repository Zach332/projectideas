package com.herokuapp.projectideas.login;

import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Optional;

import com.herokuapp.projectideas.database.documents.User;
import com.herokuapp.projectideas.database.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @Autowired
    UserRepository userRepository;

    public User getUserByEmail(String email) {
        Iterator<User> iterator = userRepository.findByEmail(email).iterator();
        if (!iterator.hasNext()) {
            return userRepository.save(new User(generateUsername(), email));
        }
        return iterator.next();
    }

    private String generateUsername() {
        try {
            RandomAccessFile adjectives = new RandomAccessFile("C:\\Users\\Zachary\\Documents\\cs-project\\projectideas\\src\\main\\resources\\adjectives.txt", "r");
            RandomAccessFile nouns = new RandomAccessFile("C:\\Users\\Zachary\\Documents\\cs-project\\projectideas\\src\\main\\resources\\nouns.txt", "r");
            System.out.println(adjectives.readLine().replace("\n", "")+nouns.readLine().replace("\n", ""));
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