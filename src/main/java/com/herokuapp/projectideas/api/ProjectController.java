package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.project.CreateProjectDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectPageDTO;
import com.herokuapp.projectideas.dto.project.ViewProjectDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProjectController {

    @Autowired
    Database database;

    @Autowired
    DTOMapper mapper;

    @GetMapping("/api/projects/tags")
    public PreviewProjectPageDTO getProjectsByTag(
        @RequestHeader(value = "authorization", required = false) String userId,
        @RequestParam("page") int pageNum,
        @RequestParam("tag") String tag
    ) {
        int lastPageNum = database.getLastPageNumForProjectTag(tag);

        List<PreviewProjectDTO> projectPreviews;
        if (pageNum <= 0 || pageNum > lastPageNum) {
            projectPreviews = new ArrayList<>();
        } else {
            projectPreviews =
                database
                    .findProjectsByTagAndPageNum(tag, pageNum)
                    .stream()
                    .map(idea -> mapper.previewProjectDTO(idea, userId))
                    .collect(Collectors.toList());
        }
        return new PreviewProjectPageDTO(
            projectPreviews,
            pageNum == lastPageNum
        );
    }

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

        if (project.userIsTeamMember(userId)) {
            return mapper.viewProjectAsTeamMemberDTO(project, userId);
        } else if (project.userHasRequestedToJoin(userId)) {
            return mapper.viewProjectDTO(project, userId);
        } else {
            return mapper.viewProjectDTO(project, userId);
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
        if (!existingProject.userIsTeamMember(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        mapper.updateProjectFromDTO(existingProject, project);
        database.updateProject(existingProject);
    }

    @PutMapping("/api/projects/{projectId}/update")
    public void updateLookingForMembers(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @RequestParam("lookingForMembers") boolean lookingForMembers
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
        if (!existingProject.userIsTeamMember(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        existingProject.setLookingForMembers(lookingForMembers);
        database.updateProject(existingProject);
    }

    @PostMapping("/api/projects/{projectId}/joinrequests")
    public void requestToJoinProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId
    ) {
        Project project = database.getProject(projectId).get();

        if (!project.isLookingForMembers()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This project is not looking for new members."
            );
        }

        User user = database
            .findUser(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.FORBIDDEN)
            );

        // Do not add request for existing memeber
        if (project.userIsTeamMember(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Do not duplicate a request to join
        if (
            project
                .getUsersRequestingToJoin()
                .stream()
                .anyMatch(
                    usernameIdPair -> usernameIdPair.getUserId().equals(userId)
                )
        ) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "User " +
                user.getUsername() +
                " has already requested to join this project"
            );
        }

        project.getUsersRequestingToJoin().add(new UsernameIdPair(user));
        database.updateProject(project);
        database.sendGroupAdminMessage(
            projectId,
            user.getUsername() +
            " has requested to join your " +
            project.getName() +
            " project. Visit your project page to accept or decline this request."
        );
    }

    @PostMapping(
        "/api/projects/{projectId}/joinrequests/{newTeamMemberUsername}"
    )
    public void respondToProjectJoinRequest(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @PathVariable String newTeamMemberUsername,
        @RequestParam("accept") boolean accept
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

        if (!project.userIsTeamMember(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        project
            .getUsersRequestingToJoin()
            .removeIf(
                usernameIdPair ->
                    usernameIdPair.getUserId().equals(newTeamMember.getUserId())
            );

        if (accept) {
            project.getTeamMembers().add(new UsernameIdPair(newTeamMember));
            newTeamMember.getJoinedProjectIds().add(projectId);
            database.updateUser(newTeamMember.getId(), newTeamMember);
            database.sendIndividualAdminMessage(
                newTeamMember.getUserId(),
                "Your request to join " +
                project.getName() +
                " has been accepted."
            );
        } else {
            database.sendIndividualAdminMessage(
                newTeamMember.getUserId(),
                "Your request to join " +
                project.getName() +
                " has been rejected."
            );
        }

        database.updateProject(project);
    }

    @PostMapping("/api/projects/{projectId}/leave")
    public void leaveProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId
    ) {
        Project project = database.getProject(projectId).get();

        if (!project.userIsTeamMember(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        project
            .getTeamMembers()
            .removeIf(
                usernameIdPair -> usernameIdPair.getUserId().equals(userId)
            );
        if (project.getTeamMembers().size() == 0) {
            database.deleteProject(projectId);
        } else {
            database.updateProject(project);
        }

        User user = database.findUser(userId).get();
        user.getJoinedProjectIds().remove(projectId);
        database.updateUser(userId, user);
    }
}
