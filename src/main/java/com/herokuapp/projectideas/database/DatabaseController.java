package com.herokuapp.projectideas.database;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.documents.Idea;
import com.herokuapp.projectideas.database.documents.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DatabaseController {

    @Autowired
    private Database database;

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

    @GetMapping("/api/ideas")
    @JsonView(View.Get.class)
    public List<Idea> getAllIdeas() {
        List<Idea> allIdeas =  database.findAllIdeas();
        Collections.reverse(allIdeas);
        return allIdeas;
    }

    @GetMapping("/api/ideas/{id}")
    @JsonView(View.Get.class)
    public Optional<Idea> getIdea(@PathVariable String id) {
        return database.findIdea(id);
    }

    @PostMapping("/api/ideas")
    public void createIdea(@RequestHeader("authorization") String userId, @RequestBody @JsonView(View.Post.class) Idea idea) {
        // TODO: Use username from database, not DTO
        Optional<User> user = database.findUser(userId);
        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.createIdea(new Idea(userId, user.get().getUsername(), idea.getTitle(), idea.getContent()));
    }

    @PutMapping("/api/ideas/{id}")
    public void updateIdea(@RequestHeader("authorization") String userId, @PathVariable String id, @RequestBody @JsonView(View.Post.class) Idea idea) {
        Optional<Idea> existingIdea = database.findIdea(id);
        Optional<User> user = database.findUser(userId);
        if (!user.isPresent() || !existingIdea.get().getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        existingIdea.get().setTitle(idea.getTitle());
        existingIdea.get().setContent(idea.getContent());
        database.updateIdea(id, existingIdea.get());
    }

    @DeleteMapping("/api/ideas/{id}")
    public void deleteIdea(@RequestHeader("authorization") String userId, @PathVariable String id) {
        Optional<Idea> ideaToDelete = database.findIdea(id);
        Optional<User> user = database.findUser(userId);
        if(!ideaToDelete.isPresent()) { return; }
        if(!user.isPresent() || !ideaToDelete.get().getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteIdea(id);
    }
}
