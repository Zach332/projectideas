package com.herokuapp.projectideas.database;

import com.herokuapp.projectideas.database.documents.Post;
import com.herokuapp.projectideas.database.documents.User;
import com.herokuapp.projectideas.database.repositories.PostRepository;
import com.herokuapp.projectideas.database.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/api/posts")
    public Iterable<Post> getAllPosts() {
        return postRepository.findAll();
    }
}
