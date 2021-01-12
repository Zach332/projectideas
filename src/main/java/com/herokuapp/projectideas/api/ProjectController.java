package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.user.User;
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
            .getTeamMemberIds()
            .stream()
            // TODO: Use denormalized usernames in place of this database call
            .map(id -> database.getUsernameFromId(id))
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
        if (!existingProject.getTeamMemberIds().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        mapper.updateProjectFromDTO(existingProject, project);
        database.updateProject(existingProject);
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
        if (project.getTeamMemberIds().contains(userId)) {
            project.getTeamMemberIds().add(newTeamMember.getUserId());
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
            !projectToDelete.getTeamMemberIds().contains(userId) &&
            !database.isUserAdmin(userId)
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteProject(projectId, projectToDelete.getIdeaId());
    }
}
