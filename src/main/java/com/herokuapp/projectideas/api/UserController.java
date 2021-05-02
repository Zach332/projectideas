package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.exception.EmptyPointReadException;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.post.PreviewIdeaPageDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectPageDTO;
import com.herokuapp.projectideas.dto.user.UpdateUserDTO;
import com.herokuapp.projectideas.dto.user.ViewUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
        try {
            User user = database.getUser(id);
            return mapper.viewUserDTO(user);
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User " + id + " does not exist."
            );
        }
    }

    @GetMapping("/api/users/{userId}/postedideas")
    public PreviewIdeaPageDTO getPostedIdeas(
        @PathVariable String userId,
        @RequestParam("page") int pageNum
    ) {
        return mapper.previewIdeaPageDTO(
            database.getPostedIdeasForUser(userId, pageNum),
            userId,
            database
        );
    }

    @GetMapping("/api/users/{userId}/savedIdeas")
    public PreviewIdeaPageDTO getSavedIdeas(
        @PathVariable String userId,
        @RequestParam("page") int pageNum
    ) {
        return mapper.previewIdeaPageDTO(
            database.getSavedIdeasForUser(userId, pageNum),
            userId,
            database
        );
    }

    @GetMapping("/api/users/{userId}/projects")
    public PreviewProjectPageDTO getJoinedProjects(
        @PathVariable String userId,
        @RequestParam("page") int pageNum
    ) {
        return mapper.previewProjectPageDTO(
            database.getJoinedProjectsForUser(userId, pageNum),
            userId,
            database
        );
    }

    @PutMapping("/api/users/{id}")
    public void updateUser(
        @PathVariable String id,
        @RequestBody UpdateUserDTO user
    ) {
        try {
            User existingUser = database.getUser(id);
            if (!existingUser.getUsername().equals(user.getUsername())) {
                if (
                    user.getUsername().length() < 3 ||
                    user.getUsername().length() > 30
                ) {
                    throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Username " +
                        user.getUsername() +
                        " is too long or too short. " +
                        "Usernames must be between 3 and 30 characters (inclusive)."
                    );
                }
                if (database.userWithUsernameExists(user.getUsername())) {
                    throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Username " + user.getUsername() + " is already taken."
                    );
                }
            }
            mapper.updateUserFromDTO(existingUser, user);
            database.updateUser(id, existingUser);
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User " + id + " does not exist."
            );
        }
    }
}
