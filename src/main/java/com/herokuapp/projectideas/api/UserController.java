package com.herokuapp.projectideas.api;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.View;
import com.herokuapp.projectideas.database.documents.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    
    @Autowired
    Database database;

    @GetMapping("/api/users/{id}")
    @JsonView(View.Get.class)
    public Optional<User> getUser(@PathVariable String id) {
        return database.findUser(id);
    }

    @PostMapping("/api/users")
    public void createUser(@RequestBody @JsonView(View.Post.class) User user) {
        // TODO: Validate input (e.g. username already taken)
        database.createUser(new User(user.getUsername(), user.getEmail()));
    }

    @PutMapping("/api/users/{id}")
    public void updateUser(@PathVariable String id, @RequestBody @JsonView(View.Post.class) User user) {
        // No authorization because ID in path verifies identity
        // TODO: validate that username is unique. Also check that the username actually changed
        Optional<User> existingUser = database.findUser(id);
        if (existingUser.isPresent()) {
            existingUser.get().setUsername(user.getUsername());
            existingUser.get().setEmail(user.getEmail());
            database.updateUser(id, existingUser.get());
        }
    }
}
