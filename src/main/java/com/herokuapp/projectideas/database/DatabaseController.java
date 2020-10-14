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
    
    @GetMapping("/api/test_database")
    public String testDatabase() {
        // User user = new User("test-username", "test-email");

        // userRepository.save(user);

        Post post = new Post("7179fa4d-a95a-4b4e-9943-b401e7ace69e", "Hello again, world");

        postRepository.save(post);

        return "Added post entry to database";
    }

    @GetMapping("/api/posts/all")
    public Iterable<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @GetMapping("/api/posts/{id}")
    public Optional<Post> getPost(@PathVariable String id) {
        return postRepository.findById(id);
    }

    @PostMapping("api/posts")
    public void createPost(@RequestBody Post post) {
        postRepository.save(post);
    }

    @DeleteMapping("/api/posts/{id}")
    public void deletePost(@PathVariable String id) {
        postRepository.deleteById(id);
    }
}
