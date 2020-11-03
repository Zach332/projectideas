package com.herokuapp.projectideas.login;

import java.util.Optional;

import com.herokuapp.projectideas.database.documents.User;
import com.herokuapp.projectideas.database.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;

public class LoginController {

    @Autowired
    UserRepository userRepository;

    public String getUserUUIDByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (!user.isPresent()) {
            return userRepository.save(new User("TODO: REPLACE THIS USERNAME WITH SOMETHING", email)).getId();
        }
        return user.get().getId();
    }
}
