package com.herokuapp.projectideas;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import okhttp3.*;

@RestController
public class GithubAuthenticationController {
    private static final OkHttpClient httpClient = new OkHttpClient();

    static class GitHubCode {
        String code;
        public GitHubCode() {
        }
        public void setCode(String code) {
            this.code = code;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GitHubUserID {
        String id;
        public GitHubUserID() {
        }
        public void setId(String id) {
            this.id = id;
        }
    }

    @PostMapping("/api/login/github")
    public GitHubUserID githubAuthentication(@RequestBody GitHubCode code) throws Exception {
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
            if (!response.isSuccessful()) {
                System.out.println("Unexpected code " + response);
                return null;
            }
            HttpUrl url = HttpUrl.parse("https://randomaddress.com/search?"+response.body().string());
            String token = url.queryParameter("access_token");
            String userResponse = getUserData(token);

            ObjectMapper mapper = new ObjectMapper();
            GitHubUserID userID = mapper.readValue(userResponse, GitHubUserID.class);

            System.out.println("Returning "+ userID.id);
            return userID;
        } catch (Exception e) {
            return null;
        }
    }

    static String getUserData(String token) throws IOException {
        Request request = new Request.Builder()
            .url("https://api.github.com/user")
            .header("Authorization","token " + token)
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Unexpected code " + response);
                return null;
            }
            return response.body().string();
        }
    }
}