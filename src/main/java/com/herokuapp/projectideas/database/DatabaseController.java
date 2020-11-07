package com.herokuapp.projectideas.database;

import java.util.Optional;

import com.herokuapp.projectideas.database.documents.Idea;
import com.herokuapp.projectideas.database.documents.User;
import com.herokuapp.projectideas.database.repositories.IdeaRepository;
import com.herokuapp.projectideas.database.repositories.UserRepository;

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
    private UserRepository userRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @GetMapping("/api/users")
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/api/users/{id}")
    public Optional<User> getUser(@PathVariable String id) {
        return userRepository.findById(id);
    }

    @PutMapping("/api/users/{id}")
    public void updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            user.get().setUsername(userDTO.username);
            user.get().setEmail(userDTO.email);
            userRepository.save(user.get());
        }
    }

    @PostMapping("/api/users")
    public void createUser(@RequestBody UserDTO userDTO) {
        // TODO: Validate input (e.g. username already taken)
        User user = new User(userDTO.username, userDTO.email);
        userRepository.save(user);
    }

    @GetMapping("/api/ideas")
    public Iterable<Idea> getAllIdeas() {
        return ideaRepository.findAll();
    }

    @GetMapping("/api/ideas/{id}")
    public Optional<Idea> getIdea(@PathVariable String id) {
        return ideaRepository.findById(id);
    }

    @PostMapping("/api/ideas")
    public void createIdea(@RequestBody IdeaDTO ideaDTO) {
        Idea idea = new Idea(ideaDTO.authorUsername, ideaDTO.title, ideaDTO.content);
        ideaRepository.save(idea);
    }

    @DeleteMapping("/api/ideas/{id}")
    public void deleteIdea(@PathVariable String id) {
        ideaRepository.deleteById(id);
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
