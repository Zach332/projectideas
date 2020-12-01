package com.herokuapp.projectideas.api;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.View;
import com.herokuapp.projectideas.database.document.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {
    
    @Autowired
    Database database;

    @GetMapping("/api/users/{id}")
    @JsonView(View.Get.class)
    public User getUser(@PathVariable String id) {
        return database.findUser(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " does not exist."));
    }

    @PostMapping("/api/users")
    public void createUser(@RequestBody @JsonView(View.Post.class) User user) {
        if (database.containsUserWithUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username " + user.getUsername() + " is already taken.");
        }
        database.createUser(new User(user.getUsername(), user.getEmail()));
    }

    @PutMapping("/api/users/{id}")
    public void updateUser(@PathVariable String id, @RequestBody @JsonView(View.Post.class) User user) {
        // No authorization because ID in path verifies identity
        User existingUser = database.findUser(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " does not exist."));
        if (!existingUser.getUsername().equals(user.getUsername()) && database.containsUserWithUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username " + user.getUsername() + " is already taken.");
        }
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        database.updateUser(id, existingUser);
    }
}
