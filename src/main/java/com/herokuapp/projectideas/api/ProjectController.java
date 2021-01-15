package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.document.user.UserIdPair;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.project.CreateProjectDTO;
import com.herokuapp.projectideas.dto.project.ViewProjectDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProjectController {

    @Autowired
    Database database;

    @Autowired
    DTOMapper mapper;

    // TODO: Handle team members differently than other users
    @GetMapping("/api/projects/{projectId}")
    public ViewProjectDTO getProject(@PathVariable String projectId) {
        Project project = database
            .getProject(projectId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project " + projectId + " does not exist."
                    )
            );
        List<String> teamMemberUsernames = project
            .getTeamMembers()
            .stream()
            .map(userIdPair -> userIdPair.getUsername())
            .collect(Collectors.toList());
        return mapper.viewProjectDTO(project, teamMemberUsernames);
    }

    @PutMapping("/api/projects/{projectId}")
    public void updateProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @RequestBody CreateProjectDTO project
    ) {
        Project existingProject = database
            .getProject(projectId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project " + projectId + " does not exist."
                    )
            );
        if (
            !existingProject
                .getTeamMembers()
                .stream()
                .anyMatch(userIdPair -> userIdPair.getUserId().equals(userId))
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        mapper.updateProjectFromDTO(existingProject, project);
        database.updateProject(existingProject);
    }

    @PostMapping("/api/projects/{projectId}/requesttojoin")
    public void requestToJoinProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId
    ) {
        Project project = database.getProject(projectId).get();
        User user = database
            .findUser(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.FORBIDDEN)
            );

        project.getUsersRequestingToJoin().add(new UserIdPair(user));
        database.sendAdminGroupMessage(
            projectId,
            user.getUsername() +
            " has requested to join your " +
            project.getName() +
            " project. Visit your project page to accept or decline this request."
        );
    }

    @PostMapping(
        "/api/projects/{projectId}/addteammember/{newTeamMemberUsername}"
    )
    public void addTeamMemberToProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @PathVariable String newTeamMemberUsername
    ) {
        User newTeamMember = database
            .findUserByUsername(newTeamMemberUsername)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User " + newTeamMemberUsername + " does not exist."
                    )
            );
        Project project = database
            .getProject(projectId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project " + projectId + " does not exist."
                    )
            );
        if (
            project
                .getTeamMembers()
                .stream()
                .anyMatch(userIdPair -> userIdPair.getUserId().equals(userId))
        ) {
            project.getTeamMembers().add(new UserIdPair(newTeamMember));
            newTeamMember.getJoinedProjectIds().add(projectId);
            database.updateProject(project);
            database.updateUser(newTeamMember.getId(), newTeamMember);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/api/projects/{projectId}")
    public void deleteProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId
    ) {
        Project projectToDelete = database
            .getProject(projectId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project " + projectId + " does not exist."
                    )
            );
        if (
            !projectToDelete
                .getTeamMembers()
                .stream()
                .anyMatch(userIdPair -> userIdPair.getUserId().equals(userId)) &
            !database.isUserAdmin(userId)
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteProject(projectId);
    }
}
