package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.project.CreateProjectDTO;
import com.herokuapp.projectideas.dto.project.ViewProjectDTO;
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

    @GetMapping("/api/projects/{projectId}")
    public ViewProjectDTO getProject(
        @RequestHeader(value = "authorization", required = false) String userId,
        @PathVariable String projectId
    ) {
        Project project = database
            .getProject(projectId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project " + projectId + " does not exist."
                    )
            );

        if (project.isUserTeamMember(userId)) {
            return mapper.viewProjectAsTeamMemberDTO(project);
        } else {
            return mapper.viewProjectDTO(project);
        }
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
        if (!existingProject.isUserTeamMember(userId)) {
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

        // Do not add request for existing memeber
        if (project.isUserTeamMember(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        project.getUsersRequestingToJoin().add(new UsernameIdPair(user));
        database.updateProject(project);
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

        if (!project.isUserTeamMember(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        project.getTeamMembers().add(new UsernameIdPair(newTeamMember));
        project
            .getUsersRequestingToJoin()
            .removeIf(
                usernameIdPair ->
                    usernameIdPair.getUsername().equals(newTeamMemberUsername)
            );
        newTeamMember.getJoinedProjectIds().add(projectId);
        database.updateProject(project);
        database.updateUser(newTeamMember.getId(), newTeamMember);
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
            !projectToDelete.isUserTeamMember(userId) &&
            !database.isUserAdmin(userId)
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteProject(projectId);
    }
}
