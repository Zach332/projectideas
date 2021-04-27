package com.herokuapp.projectideas.api;

import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.project.ProjectJoinRequest;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import com.herokuapp.projectideas.database.exception.EmptyPointReadException;
import com.herokuapp.projectideas.database.exception.EmptySingleDocumentQueryException;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.project.PreviewProjectPageDTO;
import com.herokuapp.projectideas.dto.project.RequestToJoinProjectDTO;
import com.herokuapp.projectideas.dto.project.ViewProjectDTO;
import com.herokuapp.projectideas.search.SearchController;
import com.herokuapp.projectideas.util.ControllerUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProjectController {

    @Autowired
    Database database;

    @Autowired
    DTOMapper mapper;

    @Autowired
    SearchController searchController;

    @GetMapping("/api/projects")
    public PreviewProjectPageDTO getPublicProjects(
        @RequestHeader(value = "authorization", required = false) String userId,
        @RequestParam("page") int pageNum,
        @RequestParam(value = "sort", required = false) String sort
    ) {
        if (sort == null || sort.equals("hotness")) {
            return searchController.getProjectPageByHotness(pageNum, userId);
        }
        if (sort.equals("recency")) {
            return searchController.getProjectPageByRecency(pageNum, userId);
        }
        // remaining sort option is upvotes
        return searchController.getProjectPageByUpvotes(pageNum, userId);
    }

    @GetMapping("/api/projects/tags")
    public PreviewProjectPageDTO getProjectsByTag(
        @RequestHeader(value = "authorization", required = false) String userId,
        @RequestParam("page") int pageNum,
        @RequestParam("tag") String tag
    ) {
        return mapper.previewProjectPageDTO(
            database.getPublicProjectsByTagAndPageNum(tag, pageNum),
            userId,
            database
        );
    }

    @GetMapping("/api/projects/{projectId}")
    public ResponseEntity<ViewProjectDTO> getProject(
        @RequestHeader(value = "authorization", required = false) String userId,
        @PathVariable String projectId
    ) {
        try {
            Project project = database.getProject(projectId);

            if (!ControllerUtils.userIsAuthorizedToView(project, userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            ViewProjectDTO viewProjectDTO;
            if (ControllerUtils.userIsAuthorizedToView(project, userId)) {
                viewProjectDTO =
                    mapper.viewProjectAsTeamMemberDTO(
                        project,
                        userId,
                        database
                    );
            } else if (project.userHasRequestedToJoin(userId)) {
                viewProjectDTO =
                    mapper.viewProjectDTO(project, userId, database);
            } else {
                viewProjectDTO =
                    mapper.viewProjectDTO(project, userId, database);
            }
            return ResponseEntity
                .ok()
                .lastModified(project.getTimeLastEdited() * 1000)
                .body(viewProjectDTO);
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Project " + projectId + " does not exist."
            );
        }
    }

    @PostMapping("/api/projects/{projectId}/upvote")
    public void upvoteProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId
    ) {
        database.upvoteProject(projectId, userId);
    }

    @PostMapping("/api/projects/{projectId}/unupvote")
    public void unupvoteProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId
    ) {
        database.unupvoteProject(projectId, userId);
    }

    @PatchMapping(
        path = "/api/projects/{projectId}",
        consumes = "application/json-patch+json"
    )
    public void updateProject(
        WebRequest request,
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @RequestBody JsonPatch projectPatch
    ) {
        try {
            Project project = database.getProject(projectId);

            if (!ControllerUtils.userIsAuthorizedToEdit(project, userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            if (request.checkNotModified(project.getTimeLastEdited() * 1000)) {
                throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED,
                    "Project " +
                    projectId +
                    " has been edited since the user initially loaded it. The user will" +
                    " need to save their changes elsewhere, redownload the project, and" +
                    " resubmit their edits."
                );
            }

            Project patchedProject = mapper.getProjectFromPatch(
                project,
                projectPatch
            );

            boolean toPublic = false;
            boolean toPrivate = false;
            if (
                project.isPublicProject() && !patchedProject.isPublicProject()
            ) {
                toPrivate = true;
            }
            if (
                !project.isPublicProject() && patchedProject.isPublicProject()
            ) {
                toPublic = true;
            }

            if (
                !patchedProject.isPublicProject() &&
                patchedProject.isLookingForMembers()
            ) {
                throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "A project cannot be looking for members while private."
                );
            }

            if (patchedProject.getName().length() > 175) {
                throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Project name " +
                    patchedProject.getName() +
                    " is too long. " +
                    "Project names cannot be longer than 175 characters."
                );
            }

            List<String> addedTags = ControllerUtils.getTagsOnlyInFirstDocument(
                patchedProject,
                project
            );
            List<String> removedTags = ControllerUtils.getTagsOnlyInFirstDocument(
                project,
                patchedProject
            );
            database.updateProjectWithConcurrencyControl(
                patchedProject,
                toPublic,
                toPrivate,
                addedTags,
                removedTags
            );
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Project " + projectId + " does not exist."
            );
        } catch (IllegalArgumentException | JsonPatchException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Patch was formatted incorrectly. The project was not updated."
            );
        }
    }

    @PutMapping("/api/projects/{projectId}/updatelink")
    public void updateLink(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @RequestParam("link") String link
    ) {
        try {
            Project existingProject = database.getProject(projectId);
            if (
                !ControllerUtils.userIsAuthorizedToEdit(existingProject, userId)
            ) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            existingProject.setGithubLink(link);
            database.updateProject(existingProject, false, false, null, null);
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Project " + projectId + " does not exist."
            );
        }
    }

    @PutMapping("/api/projects/{projectId}/updatelookingformembers")
    public void updateLookingForMembers(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @RequestParam("lookingForMembers") boolean lookingForMembers
    ) {
        try {
            Project existingProject = database.getProject(projectId);

            if (
                !ControllerUtils.userIsAuthorizedToEdit(existingProject, userId)
            ) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            if (lookingForMembers) {
                existingProject.setPublicProject(true);
            }
            existingProject.setLookingForMembers(lookingForMembers);
            database.updateProject(existingProject, false, false, null, null);
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Project " + projectId + " does not exist."
            );
        }
    }

    @PutMapping("/api/projects/{projectId}/updatepublicstatus")
    public void updatePublicProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @RequestParam("publicProject") boolean publicProject
    ) {
        try {
            Project existingProject = database.getProject(projectId);
            if (!publicProject && existingProject.isLookingForMembers()) {
                throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "A project cannot be private while looking for members."
                );
            }
            if (
                !ControllerUtils.userIsAuthorizedToEdit(existingProject, userId)
            ) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            existingProject.setPublicProject(publicProject);
            database.updateProject(
                existingProject,
                publicProject,
                !publicProject,
                null,
                null
            );
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Project " + projectId + " does not exist."
            );
        }
    }

    @PostMapping("/api/projects/{projectId}/joinrequests")
    public void requestToJoinProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId,
        @RequestBody RequestToJoinProjectDTO request
    ) {
        try {
            Project project = database.getProject(projectId);

            if (!project.isLookingForMembers()) {
                throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "This project is not looking for new members."
                );
            }

            User user = database.getUser(userId);

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
                        usernameIdPair ->
                            usernameIdPair.getUserId().equals(userId)
                    )
            ) {
                throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "User " +
                    user.getUsername() +
                    " has already requested to join this project"
                );
            }

            project
                .getUsersRequestingToJoin()
                .add(new ProjectJoinRequest(user, request.getRequestMessage()));
            database.updateProject(project, false, false, null, null);
            database.sendGroupAdminMessage(
                projectId,
                user.getUsername() +
                " has requested to join your " +
                project.getName() +
                " project. Visit your project page to accept or decline this request."
            );
        } catch (EmptyPointReadException e) {
            if (e.getDocumentType().equals("User")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            } else {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Project " + projectId + " does not exist."
                );
            }
        }
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
        try {
            User newTeamMember = database.getUserByUsername(
                newTeamMemberUsername
            );
            Project project = database.getProject(projectId);

            if (!ControllerUtils.userIsAuthorizedToEdit(project, userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            // Do not accept request for existing memeber
            if (
                project.getTeamMemberUsernames().contains(newTeamMemberUsername)
            ) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            project
                .getUsersRequestingToJoin()
                .removeIf(
                    usernameIdPair ->
                        usernameIdPair
                            .getUserId()
                            .equals(newTeamMember.getUserId())
                );

            if (accept) {
                project.getTeamMembers().add(new UsernameIdPair(newTeamMember));

                database.joinProjectForUser(
                    newTeamMember.getUserId(),
                    projectId
                );

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

            database.updateProject(project, false, false, null, null);
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Project " + projectId + " does not exist."
            );
        } catch (EmptySingleDocumentQueryException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User " + newTeamMemberUsername + " does not exist."
            );
        }
    }

    @PostMapping("/api/projects/invites/{inviteId}/accept")
    public String acceptInvite(
        @RequestHeader("authorization") String userId,
        @PathVariable String inviteId
    ) {
        try {
            User user = database.getUser(userId);
            Project project = database.getProjectByInviteId(inviteId);

            // Do not accept invite for existing memeber
            if (project.userIsTeamMember(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            project.getTeamMembers().add(new UsernameIdPair(user));

            database.joinProjectForUser(
                user.getUserId(),
                project.getProjectId()
            );

            database.sendGroupAdminMessage(
                project.getProjectId(),
                user.getUsername() +
                " has accepted an invite to join " +
                project.getName()
            );

            database.updateProject(project, false, false, null, null);

            return project.getProjectId();
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (EmptySingleDocumentQueryException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Invite id " + inviteId + " does not correspond to a project."
            );
        }
    }

    @PostMapping("/api/projects/{projectId}/leave")
    public void leaveProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String projectId
    ) {
        try {
            Project project = database.getProject(projectId);

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
                database.updateProject(project, false, false, null, null);
            }

            database.leaveProjectForUser(userId, projectId);
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Project " + projectId + " does not exist."
            );
        }
    }

    // TODO: Rename function
    @GetMapping("/api/projects/search")
    public PreviewProjectPageDTO searchIdeas(
        @RequestHeader(value = "authorization", required = false) String userId,
        @RequestParam("query") String query,
        @RequestParam("page") int page
    ) {
        return searchController.searchForProjectByPage(query, page, userId);
    }
}
