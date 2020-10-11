package com.herokuapp.projectideas;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GithubAuthenticationController {
    
    @PostMapping("/api/login/github")
    public String githubAuthentication(@RequestBody String code) {
        return "hello world";
    }
}
