package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.post.Comment;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.post.PostCommentDTO;
import com.herokuapp.projectideas.dto.post.PostIdeaDTO;
import com.herokuapp.projectideas.dto.post.PreviewIdeaPageDTO;
import com.herokuapp.projectideas.dto.post.ViewCommentDTO;
import com.herokuapp.projectideas.dto.post.ViewIdeaDTO;
import com.herokuapp.projectideas.dto.project.CreateProjectDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectDTO;
import com.herokuapp.projectideas.search.SearchController;
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
    public PreviewIdeaPageDTO getIdeas(@RequestParam("page") int pageNum) {
        return mapper.previewIdeaPageDTO(database.getIdeasByPageNum(pageNum));
    }

    @GetMapping("/api/ideas/tags")
    public PreviewIdeaPageDTO getIdeasByTag(
        @RequestParam("page") int pageNum,
        @RequestParam("tag") String tag
    ) {
        return mapper.previewIdeaPageDTO(
            database.getIdeasByTagAndPageNum(tag, pageNum)
        );
    }

    @GetMapping("/api/ideas/{ideaId}")
    public ViewIdeaDTO getIdea(
        @PathVariable String ideaId,
        @RequestHeader(value = "authorization", required = false) String userId
    ) {
        Idea idea = database
            .getIdea(ideaId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Idea " + ideaId + " does not exist."
                    )
            );
        Boolean ideaSavedByUser;
        if (userId != null && !userId.equals("")) {
            ideaSavedByUser = database.isIdeaSavedByUser(userId, ideaId);
        } else {
            ideaSavedByUser = null;
        }
        return mapper.viewIdeaDTO(idea, ideaSavedByUser);
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
                .getPublicProjectsLookingForMemberBasedOnIdea(ideaId)
                .stream()
                .map(project -> mapper.previewProjectDTO(project, userId))
                .collect(Collectors.toList());
        } else {
            return database
                .getProjectsBasedOnIdea(ideaId)
                .stream()
                .map(project -> mapper.previewProjectDTO(project, userId))
                .collect(Collectors.toList());
        }
    }

    @PostMapping("/api/ideas")
    public void createIdea(
        @RequestHeader("authorization") String userId,
        @RequestBody PostIdeaDTO idea
    ) {
        // TODO: Move this call into the createIdea function
        // As is, creating an idea requires calling the findUser function twice
        User user = database
            .getUser(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.FORBIDDEN)
            );
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
        @RequestParam("query") String query,
        @RequestParam("page") int page
    ) {
        return searchController.searchForIdeaByPage(query, page);
    }

    @PostMapping("/api/ideas/{ideaId}/comments")
    public void createComment(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId,
        @RequestBody PostCommentDTO comment
    ) {
        User user = database
            .getUser(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.FORBIDDEN)
            );
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
    public void createProject(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId,
        @RequestBody CreateProjectDTO project
    ) {
        User user = database.getUser(userId).get();

        if (!project.isPublicProject() && project.isLookingForMembers()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A project cannot be looking for members while private."
            );
        }

        database.createProject(
            new Project(
                project.getName(),
                project.getDescription(),
                ideaId,
                new UsernameIdPair(user),
                project.isPublicProject(),
                project.isLookingForMembers(),
                project.getTags()
            ),
            userId
        );
    }

    @PutMapping("/api/ideas/{id}")
    public void updateIdea(
        @RequestHeader("authorization") String userId,
        @PathVariable String id,
        @RequestBody PostIdeaDTO idea
    ) {
        Idea existingIdea = database
            .getIdea(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Idea " + id + " does not exist."
                    )
            );
        if (
            !existingIdea.getAuthorId().equals(userId) &&
            !database.isUserAdmin(userId)
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        mapper.updateIdeaFromDTO(existingIdea, idea);
        database.updateIdea(existingIdea);
    }

    @PutMapping("/api/ideas/{ideaId}/comments/{commentId}")
    public void updateComment(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId,
        @PathVariable String commentId,
        @RequestBody PostCommentDTO comment
    ) {
        Comment existingComment = database
            .getCommentOnIdea(ideaId, commentId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comment " + commentId + " does not exist."
                    )
            );
        if (
            !existingComment.getAuthorId().equals(userId) &&
            !database.isUserAdmin(userId)
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        mapper.updateCommentFromDTO(existingComment, comment);
        database.updateComment(existingComment);
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
    ) {
        Idea ideaToDelete = database
            .getIdea(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Idea " + id + " does not exist."
                    )
            );
        if (
            !ideaToDelete.getAuthorId().equals(userId) &&
            !database.isUserAdmin(userId)
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteIdea(ideaToDelete);
    }

    @DeleteMapping("/api/ideas/{ideaId}/comments/{commentId}")
    public void deleteComment(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId,
        @PathVariable String commentId
    ) {
        Comment commentToDelete = database
            .getCommentOnIdea(ideaId, commentId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comment " + commentId + " does not exist."
                    )
            );
        if (
            !commentToDelete.getAuthorId().equals(userId) &&
            !database.isUserAdmin(userId)
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteComment(commentId, ideaId);
    }
}
