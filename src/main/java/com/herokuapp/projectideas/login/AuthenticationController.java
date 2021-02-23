package com.herokuapp.projectideas.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.herokuapp.projectideas.database.document.user.User;
import java.io.IOException;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    @Autowired
    LoginController loginController;

    private static final OkHttpClient httpClient = new OkHttpClient();

    static class GitHubCode {

        String code;

        public GitHubCode() {}

        public void setCode(String code) {
            this.code = code;
        }
    }

    static class GoogleToken {

        String token;

        public GoogleToken() {}

        public void setToken(String token) {
            this.token = token;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GithubEmail {

        public String email;
        public boolean primary;
        public boolean verified;

        public GithubEmail() {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GoogleEmail {

        public String email;

        public GoogleEmail() {}
    }

    static class UserDTO {

        public String id;
        public String username;
        public String email;
        public long timeCreated;
        public boolean admin;

        public UserDTO() {}
    }

    @PostMapping("/api/login/google")
    public UserDTO googleAuthentication(@RequestBody GoogleToken googleToken)
        throws IOException {
        Request request = new Request.Builder()
            .url("https://www.googleapis.com/oauth2/v1/userinfo")
            .header("Authorization", "Bearer " + googleToken.token)
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Unexpected code " + response);
                return null;
            }
            String responseString = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            GoogleEmail email = mapper.readValue(
                responseString,
                GoogleEmail.class
            );

            return convertUserToDTO(
                loginController.getUserByEmail(email.email)
            );
        }
    }

    @PostMapping("/api/login/github")
    public UserDTO githubAuthentication(@RequestBody GitHubCode code) {
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
            HttpUrl url = HttpUrl.parse(
                "https://randomaddress.com/search?" + response.body().string()
            );
            String token = url.queryParameter("access_token");
            String userResponse = getUserEmail(token);

            ObjectMapper mapper = new ObjectMapper();
            GithubEmail[] emails = mapper.readValue(
                userResponse,
                GithubEmail[].class
            );
            GithubEmail primary = getPrimary(emails);

            return convertUserToDTO(
                loginController.getUserByEmail(primary.email)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static String getUserEmail(String token) throws IOException {
        Request request = new Request.Builder()
            .url("https://api.github.com/user/emails")
            .header("Authorization", "token " + token)
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Unexpected code " + response);
                return null;
            }
            return response.body().string();
        }
    }

    static GithubEmail getPrimary(GithubEmail[] emails) {
        for (GithubEmail email : emails) {
            if (email.primary) return email;
        }
        return emails[0];
    }

    static UserDTO convertUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.id = user.getId();
        userDTO.username = user.getUsername();
        userDTO.email = user.getEmail();
        userDTO.timeCreated = user.getTimeCreated();
        userDTO.admin = user.isAdmin();
        return userDTO;
    }
}
