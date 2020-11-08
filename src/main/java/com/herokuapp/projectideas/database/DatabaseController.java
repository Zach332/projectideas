package com.herokuapp.projectideas.database;

import java.util.List;
import java.util.Optional;

import com.herokuapp.projectideas.database.documents.Idea;
import com.herokuapp.projectideas.database.documents.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseController {

    @Autowired
    private Database database;

    @GetMapping("/api/users")
    public List<User> getAllUsers() {
        return database.findAllUsers();
    }

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
    public void createIdea(@RequestBody IdeaDTO ideaDTO) {
        Idea idea = new Idea(ideaDTO.authorUsername, ideaDTO.title, ideaDTO.content);
        database.createIdea(idea);
    }

    @DeleteMapping("/api/ideas/{id}")
    public void deleteIdea(@PathVariable String id) {
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
