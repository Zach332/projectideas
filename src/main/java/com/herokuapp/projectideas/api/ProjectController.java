package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.project.ProjectJoinRequest;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.project.CreateProjectDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectPageDTO;
import com.herokuapp.projectideas.dto.project.RequestToJoinProjectDTO;
import com.herokuapp.projectideas.dto.project.ViewProjectDTO;
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

    @GetMapping("/api/projects")
    public PreviewProjectPageDTO getPublicProjects(
        @RequestHeader(value = "authorization", required = false) String userId,
        @RequestParam("page") int pageNum
    ) {
        return mapper.previewProjectPageDTO(
            database.getPublicProjectsByPageNum(pageNum)
        );
    }

    @GetMapping("/api/projects/tags")
    public PreviewProjectPageDTO getProjectsByTag(
        @RequestHeader(value = "authorization", required = false) String userId,
        @RequestParam("page") int pageNum,
        @RequestParam("tag") String tag
    ) {
        return mapper.previewProjectPageDTO(
            database.getPublicProjectsByTagAndPageNum(tag, pageNum)
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
        if (!project.isPublicProject() && project.isLookingForMembers()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A project cannot be looking for members while private."
            );
        }

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
        if (lookingForMembers) {
            existingProject.setPublicProject(true);
        }
        existingProject.setLookingForMembers(lookingForMembers);
        database.updateProject(existingProject);
    }

    @PutMapping("/api/projects/{projectId}/updatepublicstatus")
    public void updatePublicProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @RequestParam("publicProject") boolean publicProject
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
        if (!publicProject && existingProject.isLookingForMembers()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A project cannot be private while looking for members."
            );
        }
        if (!existingProject.userIsTeamMember(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        existingProject.setPublicProject(publicProject);
        database.updateProject(existingProject);
    }

    @PostMapping("/api/projects/{projectId}/joinrequests")
    public void requestToJoinProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @RequestBody RequestToJoinProjectDTO request
    ) {
        Project project = database.getProject(projectId).get();

        if (!project.isLookingForMembers()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This project is not looking for new members."
            );
        }

        User user = database
            .getUser(userId)
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

        project
            .getUsersRequestingToJoin()
            .add(new ProjectJoinRequest(user, request.getRequestMessage()));
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
            .getUserByUsername(newTeamMemberUsername)
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

        // Do not accept request for existing memeber
        if (project.getTeamMemberUsernames().contains(newTeamMemberUsername)) {
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

        User user = database.getUser(userId).get();
        user.getJoinedProjectIds().remove(projectId);
        database.updateUser(userId, user);
    }
}
