package com.herokuapp.projectideas.database;

import java.util.List;
import java.util.Optional;

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
    public Optional<User> getUser(@PathVariable String id) {
        return database.findUser(id);
    }

    @PostMapping("/api/users")
    public void createUser(@RequestBody UserDTO userDTO) {
        // TODO: Validate input (e.g. username already taken)
        User user = new User(userDTO.username, userDTO.email);
        database.createUser(user);
    }

    @PutMapping("/api/users/{id}")
    public void updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        // TODO: validate that username is unique. Also check that the username actually changed
        Optional<User> user = database.findUser(id);
        if (user.isPresent()) {
            user.get().setUsername(userDTO.username);
            user.get().setEmail(userDTO.email);
            database.updateUser(id, user.get());
        }
    }

    @GetMapping("/api/ideas")
    public List<Idea> getAllIdeas() {
        return database.findAllIdeas();
    }

    @GetMapping("/api/ideas/{id}")
    public Optional<Idea> getIdea(@PathVariable String id) {
        return database.findIdea(id);
    }

    @PostMapping("/api/ideas")
    public void createIdea(@RequestHeader("authorization") String userId, @RequestBody IdeaDTO ideaDTO) {
        Optional<User> user = database.findUser(userId);
        if(!user.isPresent() || !user.get().getUsername().equals(ideaDTO.authorUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Idea idea = new Idea(userId, ideaDTO.authorUsername, ideaDTO.title, ideaDTO.content);
        database.createIdea(idea);
    }

    @DeleteMapping("/api/ideas/{id}")
    public void deleteIdea(@RequestHeader("authorization") String userId, @PathVariable String id) {
        Optional<Idea> ideaToDelete = database.findIdea(id);
        Optional<User> user = database.findUser(userId);
        if(!ideaToDelete.isPresent()) { return; }
        if(!user.isPresent() || !user.get().getUsername().equals(ideaToDelete.get().getAuthorUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteIdea(id);
    }

    static class IdeaDTO {
        public String authorUsername;
        public String title;
        public String content;

        public IdeaDTO() { }
    }

    static class UserDTO {
        public String username;
        public String email;

        public UserDTO() { }
    }
}
