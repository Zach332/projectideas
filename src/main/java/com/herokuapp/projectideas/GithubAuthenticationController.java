package com.herokuapp.projectideas;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5000" })
@RestController
public class GithubAuthenticationController {
    
    @PostMapping("/api/login/github")
    public String githubAuthentication(@RequestBody String code) {
        return "hello world";
    }
}
