package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.post.PreviewIdeaDTO;
import com.herokuapp.projectideas.dto.user.CreateUserDTO;
import com.herokuapp.projectideas.dto.user.ViewUserDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {

    @Autowired
    Database database;

    @Autowired
    DTOMapper mapper;

    @GetMapping("/api/users/{id}")
    public ViewUserDTO getUser(@PathVariable String id) {
        User user = database
            .findUser(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User " + id + " does not exist."
                    )
            );
        return mapper.viewUserDTO(user);
    }

    @GetMapping("/api/users/{userId}/postedideas")
    public List<PreviewIdeaDTO> getPostedIdeas(@PathVariable String userId) {
        return database
            .getPostedIdeasForUser(userId)
            .stream()
            .map(idea -> mapper.previewIdeaDTO(idea))
            .collect(Collectors.toList());
    }

    @GetMapping("/api/users/{userId}/savedIdeas")
    public List<PreviewIdeaDTO> getSavedIdeas(@PathVariable String userId) {
        return database
            .getSavedIdeasForUser(userId)
            .stream()
            .map(idea -> mapper.previewIdeaDTO(idea))
            .collect(Collectors.toList());
    }

    @PostMapping("/api/users")
    public void createUser(@RequestBody CreateUserDTO user) {
        if (database.containsUserWithUsername(user.getUsername())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Username " + user.getUsername() + " is already taken."
            );
        }
        database.createUser(new User(user.getUsername(), user.getEmail()));
    }

    @PutMapping("/api/users/{id}")
    public void updateUser(
        @PathVariable String id,
        @RequestBody CreateUserDTO user
    ) {
        // No authorization because ID in path verifies identity
        User existingUser = database
            .findUser(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User " + id + " does not exist."
                    )
            );
        if (!existingUser.getUsername().equals(user.getUsername())) {
            if (
                user.getUsername().length() < 3 ||
                user.getUsername().length() > 30
            ) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username " +
                    user.getUsername() +
                    "is too long or too short. " +
                    "Usernames must be between 3 and 30 characters (inclusive)."
                );
            }
            if (database.containsUserWithUsername(user.getUsername())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username " + user.getUsername() + " is already taken."
                );
            }
        }
        mapper.updateUserFromDTO(existingUser, user);
        database.updateUser(id, existingUser);
    }
}
