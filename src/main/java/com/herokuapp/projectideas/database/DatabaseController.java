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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IdeaRepository ideaRepository;

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
    public IdeaDTO getIdea(@PathVariable String id) {
        Optional<Idea> idea = ideaRepository.findById(id);
        if (idea.isPresent()) {
            IdeaDTO ideaDTO = new IdeaDTO(idea.get().getContent());
            return ideaDTO;
        }
        else {
            return null;
        }
    }

    @PostMapping("/api/ideas")
    public void createIdea(@RequestBody IdeaDTO postDTO) {
        Idea idea = new Idea("test-username", postDTO.content);
        ideaRepository.save(idea);
    }

    @DeleteMapping("/api/ideas/{id}")
    public void deleteIdea(@PathVariable String id) {
        ideaRepository.deleteById(id);
    }

    static class IdeaDTO {
        private String content;

        public IdeaDTO(String content) {
            this.content = content;
        }
    }

    static class UserDTO {
        private String username;
        private String email;

        public UserDTO(String username, String email) {
            this.username = username;
            this.email = email;
        }
    }
}
