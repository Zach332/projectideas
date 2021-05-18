package com.herokuapp.projectideas.api;

import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.post.Comment;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import com.herokuapp.projectideas.database.exception.DatabaseException;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.post.PostCommentDTO;
import com.herokuapp.projectideas.dto.post.PostIdeaDTO;
import com.herokuapp.projectideas.dto.post.PreviewIdeaPageDTO;
import com.herokuapp.projectideas.dto.post.ViewCommentDTO;
import com.herokuapp.projectideas.dto.post.ViewIdeaDTO;
import com.herokuapp.projectideas.dto.project.CreateProjectDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectDTO;
import com.herokuapp.projectideas.search.SearchController;
import com.herokuapp.projectideas.util.ControllerUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class IdeaController {

    @Autowired
    Database database;

    @Autowired
    DTOMapper mapper;

    @Autowired
    SearchController searchController;

    @GetMapping("/api/ideas")
    public PreviewIdeaPageDTO getIdeas(
        @RequestHeader(value = "authorization", required = false) String userId,
        @RequestParam("page") int pageNum,
        @RequestParam(value = "sort", required = false) String sort
    ) {
        if (sort == null || sort.equals("hotness")) {
            return searchController.getIdeaPageByHotness(pageNum, userId);
        }
        if (sort.equals("recency")) {
            return searchController.getIdeaPageByRecency(pageNum, userId);
        }
        // remaining sort option is upvotes
        return searchController.getIdeaPageByUpvotes(pageNum, userId);
    }

    @GetMapping("/api/ideas/tags")
    public PreviewIdeaPageDTO getIdeasByTag(
        @RequestHeader(value = "authorization", required = false) String userId,
        @RequestParam("page") int pageNum,
        @RequestParam("tag") String tag
    ) {
        return mapper.previewIdeaPageDTO(
            database.getIdeasByTagAndPageNum(tag, pageNum),
            userId,
            database
        );
    }

    @GetMapping("/api/ideas/{ideaId}")
    public ViewIdeaDTO getIdea(
        @RequestHeader(value = "authorization", required = false) String userId,
        @PathVariable String ideaId
    ) throws DatabaseException {
        Idea idea = database.getIdea(ideaId);
        return mapper.viewIdeaDTO(idea, userId, database);
    }

    @GetMapping("/api/ideas/{ideaId}/comments")
    public List<ViewCommentDTO> getCommentsOnIdea(@PathVariable String ideaId) {
        return database
            .getAllCommentsOnIdea(ideaId)
            .stream()
            .map(comment -> mapper.viewCommentDTO(comment))
            .collect(Collectors.toList());
    }

    @GetMapping("/api/ideas/{ideaId}/projects")
    public List<PreviewProjectDTO> getProjectsBasedOnIdea(
        @RequestHeader(value = "authorization", required = false) String userId,
        @PathVariable String ideaId,
        @RequestParam(
            value = "lookingForMembersOnly",
            required = false
        ) boolean lookingForMembersOnly
    ) {
        if (lookingForMembersOnly) {
            return database
                .getPublicProjectsLookingForMembersBasedOnIdea(ideaId)
                .stream()
                .map(
                    project ->
                        mapper.previewProjectDTO(project, userId, database)
                )
                .collect(Collectors.toList());
        } else {
            return database
                .getProjectsBasedOnIdea(ideaId)
                .stream()
                .map(
                    project ->
                        mapper.previewProjectDTO(project, userId, database)
                )
                .collect(Collectors.toList());
        }
    }

    @PostMapping("/api/ideas")
    public void createIdea(
        @RequestHeader("authorization") String userId,
        @RequestBody PostIdeaDTO idea
    ) throws DatabaseException {
        if (idea.getTitle().length() > 175) {
            throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Idea title " +
                idea.getTitle() +
                " is too long. " +
                "Idea titles cannot be longer than 175 characters."
            );
        }

        User user = database.getUser(userId);
        database.createIdea(
            new Idea(
                userId,
                user.getUsername(),
                idea.getTitle(),
                idea.getContent(),
                idea.getTags()
            )
        );
    }

    @GetMapping("/api/ideas/search")
    public PreviewIdeaPageDTO searchIdeas(
        @RequestHeader(value = "authorization", required = false) String userId,
        @RequestParam("query") String query,
        @RequestParam("page") int page
    ) {
        return searchController.searchForIdeaByPage(query, userId, page);
    }

    @PostMapping("/api/ideas/{ideaId}/comments")
    public void createComment(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId,
        @RequestBody PostCommentDTO comment
    ) throws DatabaseException {
        User user = database.getUser(userId);
        database.createComment(
            new Comment(
                ideaId,
                user.getId(),
                user.getUsername(),
                comment.getContent()
            )
        );
    }

    @PostMapping("/api/ideas/{ideaId}/projects")
    public String createProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId,
        @RequestBody CreateProjectDTO project
    ) throws DatabaseException {
        if (!project.isPublicProject() && project.isLookingForMembers()) {
            throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "A project cannot be looking for members while private."
            );
        }

        if (project.getName().length() > 175) {
            throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Project name " +
                project.getName() +
                " is too long. " +
                "Project names cannot be longer than 175 characters."
            );
        }

        User user = database.getUser(userId);
        Project newProject = new Project(
            project.getName(),
            project.getDescription(),
            ideaId,
            new UsernameIdPair(user),
            project.isPublicProject(),
            project.isLookingForMembers(),
            project.getTags()
        );
        database.createProject(newProject, userId);
        return newProject.getId();
    }

    @PatchMapping(
        path = "/api/ideas/{id}",
        consumes = "application/json-patch+json"
    )
    public void updateIdea(
        @RequestHeader("authorization") String userId,
        @PathVariable String id,
        @RequestBody JsonPatch ideaPatch
    ) throws DatabaseException {
        try {
            Idea idea = database.getIdea(id);

            if (!ControllerUtils.userIsAuthorizedToEdit(idea, userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            Idea patchedIdea = mapper.getIdeaFromPatch(idea, ideaPatch);

            if (patchedIdea.getTitle().length() > 175) {
                throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Idea title " +
                    patchedIdea.getTitle() +
                    " is too long. " +
                    "Idea titles cannot be longer than 175 characters."
                );
            }

            List<String> addedTags = ControllerUtils.getTagsOnlyInFirstDocument(
                patchedIdea,
                idea
            );
            List<String> removedTags = ControllerUtils.getTagsOnlyInFirstDocument(
                idea,
                patchedIdea
            );
            database.updateIdea(patchedIdea, addedTags, removedTags);
        } catch (IllegalArgumentException | JsonPatchException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Patch was formatted incorrectly. The idea was not updated."
            );
        }
    }

    @PutMapping("/api/ideas/{ideaId}/comments/{commentId}")
    public void updateComment(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId,
        @PathVariable String commentId,
        @RequestBody PostCommentDTO comment
    ) throws DatabaseException {
        Comment existingComment = database.getCommentOnIdea(ideaId, commentId);
        if (ControllerUtils.userIsAuthorizedToEdit(existingComment, userId)) {
            mapper.updateCommentFromDTO(existingComment, comment);
            database.updateComment(existingComment);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/api/ideas/{ideaId}/save")
    public void saveIdea(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId
    ) {
        database.saveIdeaForUser(ideaId, userId);
    }

    @PostMapping("/api/ideas/{ideaId}/unsave")
    public void unsaveIdea(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId
    ) {
        database.unsaveIdeaForUser(ideaId, userId);
    }

    @PostMapping("/api/ideas/{ideaId}/upvote")
    public void upvoteIdea(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId
    ) {
        database.upvoteIdea(ideaId, userId);
    }

    @PostMapping("/api/ideas/{ideaId}/unupvote")
    public void unupvoteIdea(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId
    ) {
        database.unupvoteIdea(ideaId, userId);
    }

    @DeleteMapping("/api/ideas/{id}")
    public void deleteIdea(
        @RequestHeader("authorization") String userId,
        @PathVariable String id
    ) throws DatabaseException {
        Idea ideaToDelete = database.getIdea(id);
        if (ControllerUtils.userIsAuthorizedToEdit(ideaToDelete, userId)) {
            database.deleteIdea(ideaToDelete);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/api/ideas/{ideaId}/comments/{commentId}")
    public void deleteComment(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId,
        @PathVariable String commentId
    ) throws DatabaseException {
        Comment commentToDelete = database.getCommentOnIdea(ideaId, commentId);
        if (ControllerUtils.userIsAuthorizedToEdit(commentToDelete, userId)) {
            database.deleteComment(commentId, ideaId);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
