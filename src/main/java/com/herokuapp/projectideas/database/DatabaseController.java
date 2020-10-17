package com.herokuapp.projectideas.database;

import java.util.Optional;

import com.herokuapp.projectideas.database.documents.Post;
import com.herokuapp.projectideas.database.documents.User;
import com.herokuapp.projectideas.database.repositories.PostRepository;
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
    private PostRepository postRepository;

    @PostMapping("/api/users")
    public void createUser(@RequestBody UserDTO userDTO) {
        // TODO: Validate input (e.g. username already taken)
        User user = new User(userDTO.username, userDTO.email);
        userRepository.save(user);
    }

    @GetMapping("/api/posts/{id}")
    public Optional<Post> getPost(@PathVariable String id) {
        return postRepository.findById(id);
    }

    @PostMapping("/api/posts")
    public void createPost(@RequestBody PostDTO postDTO) {
        Post post = new Post(postDTO.authorId, postDTO.content);
        postRepository.save(post);
    }

    @DeleteMapping("/api/posts/{id}")
    public void deletePost(@PathVariable String id) {
        postRepository.deleteById(id);
    }

    static class PostDTO {
        // TODO: The authorId should be retrieved from the client adding the post
        private String authorId;
        private String content;

        public PostDTO(String authorId, String content) {
            this.authorId = authorId;
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
