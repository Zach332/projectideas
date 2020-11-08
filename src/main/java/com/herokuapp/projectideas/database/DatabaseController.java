package com.herokuapp.projectideas.database;

import java.util.Optional;
import java.util.Map;

import com.herokuapp.projectideas.database.documents.Idea;
import com.herokuapp.projectideas.database.documents.User;
import com.herokuapp.projectideas.database.repositories.IdeaRepository;
import com.herokuapp.projectideas.database.repositories.UserRepository;

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
    private UserRepository userRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @GetMapping("/api/users/{id}")
    public Optional<User> getUser(@PathVariable String id) {
        return userRepository.findById(id);
    }

    @PutMapping("/api/users/{id}")
    public void updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        Optional<User> user = userRepository.findById(id);
        //todo: validate that username is unique
        if (user.isPresent()) {
            user.get().setUsername(userDTO.username);
            user.get().setEmail(userDTO.email);
            userRepository.save(user.get());
        }
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
    public void createIdea(@RequestHeader("authorization") String userId, @RequestBody IdeaDTO ideaDTO) {
        Optional<User> user = userRepository.findById(userId);
        if(!user.isPresent() || !user.get().getUsername().equals(ideaDTO.authorUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Idea idea = new Idea(ideaDTO.authorUsername, ideaDTO.title, ideaDTO.content);
        ideaRepository.save(idea);
    }

    @DeleteMapping("/api/ideas/{id}")
    public void deleteIdea(@RequestHeader("authorization") String userId, @PathVariable String id) {
        Optional<Idea> ideaToDelete = ideaRepository.findById(id);
        Optional<User> user = userRepository.findById(userId);
        if(!ideaToDelete.isPresent()) { return; }
        if(!user.isPresent() || !user.get().getUsername().equals(ideaToDelete.get().getAuthorUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
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
