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
    static class GithubEmail {
        public String email;
        public boolean primary;
        public boolean verified;
        public GithubEmail() {
        }
    }

    @PostMapping("/api/login/github")
    public GithubEmail githubAuthentication(@RequestBody GitHubCode code) {
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
            String userResponse = getUserEmail(token);
            System.out.println(userResponse);

            ObjectMapper mapper = new ObjectMapper();
            GithubEmail[] userID = mapper.readValue(userResponse, GithubEmail[].class);

            return userID[0];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static String getUserEmail(String token) throws IOException {
        Request request = new Request.Builder()
            .url("https://api.github.com/user/emails")
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