package com.herokuapp.projectideas;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import okhttp3.*;

@RestController
public class GithubAuthenticationController {
    private final OkHttpClient httpClient = new OkHttpClient();

    static class GitHubCode {
        public GitHubCode() { }
        public void setCode(String code) {
            this.code = code;
        }
        String code;
    }

    @PostMapping("/api/login/github")
    public String githubAuthentication(@RequestBody GitHubCode code) throws Exception {
        FormBody formBody = new FormBody.Builder()
                .add("client_id", System.getenv("REACT_APP_GITHUB_CLIENT_ID"))
                .add("client_secret", System.getenv("GITHUB_CLIENT_SECRET"))
                .add("code", code.code)
                .add("redirect_uri", System.getenv("REACT_APP_GITHUB_REDIRECT_URI"))
                .build();

        Request request = new Request.Builder()
                .url("https://github.com/login/oauth/access_token")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) System.out.println("Unexpected code " + response);

            System.out.println(response.body().string());
            return response.body().string();
        } catch(Exception e) {
            return null;
        }
    }
}