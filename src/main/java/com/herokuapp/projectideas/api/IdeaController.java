package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.User;
import com.herokuapp.projectideas.database.document.post.Comment;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.tag.Tag;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.post.PostCommentDTO;
import com.herokuapp.projectideas.dto.post.PostIdeaDTO;
import com.herokuapp.projectideas.dto.post.PreviewIdeaDTO;
import com.herokuapp.projectideas.dto.post.PreviewIdeaPageDTO;
import com.herokuapp.projectideas.dto.post.ViewCommentDTO;
import com.herokuapp.projectideas.dto.post.ViewIdeaDTO;
import com.herokuapp.projectideas.search.SearchController;
import java.util.ArrayList;
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
        int lastPageNum = database.getLastPageNum();

        List<PreviewIdeaDTO> ideaPreviews;
        if (pageNum <= 0 || pageNum > lastPageNum) {
            ideaPreviews = new ArrayList<>();
        } else {
            ideaPreviews =
                database
                    .findIdeasByPageNum(pageNum)
                    .stream()
                    .map(idea -> mapper.previewIdeaDTO(idea))
                    .collect(Collectors.toList());
        }
        return new PreviewIdeaPageDTO(ideaPreviews, pageNum == lastPageNum);
    }

    @GetMapping("/api/ideas/{ideaId}")
    public ViewIdeaDTO getIdea(
        @PathVariable String ideaId,
        @RequestHeader(value = "authorization", required = false) String userId
    ) {
        Idea idea = database
            .findIdea(ideaId)
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
            .findAllCommentsOnIdea(ideaId)
            .stream()
            .map(comment -> mapper.viewCommentDTO(comment))
            .collect(Collectors.toList());
    }

    @PostMapping("/api/ideas")
    public void createIdea(
        @RequestHeader("authorization") String userId,
        @RequestBody PostIdeaDTO idea
    ) {
        User user = database
            .findUser(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.FORBIDDEN)
            );
        List<Tag> tags = idea
            .getTags()
            .stream()
            .map(tag -> new Tag(tag, Tag.Type.Idea))
            .collect(Collectors.toList());
        database.createIdea(
            new Idea(
                userId,
                user.getUsername(),
                idea.getTitle(),
                idea.getContent(),
                tags
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
            .findUser(userId)
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

    @PutMapping("/api/ideas/{id}")
    public void updateIdea(
        @RequestHeader("authorization") String userId,
        @PathVariable String id,
        @RequestBody PostIdeaDTO idea
    ) {
        Idea existingIdea = database
            .findIdea(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Idea " + id + " does not exist."
                    )
            );
        User user = database
            .findUser(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.FORBIDDEN)
            );
        if (!user.isAdmin() && !existingIdea.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        existingIdea.setTitle(idea.getTitle());
        existingIdea.setContent(idea.getContent());
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
            .findCommentOnIdea(ideaId, commentId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comment " + commentId + " does not exist."
                    )
            );
        if (!existingComment.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        existingComment.setContent(comment.getContent());
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

    @DeleteMapping("/api/ideas/{id}")
    public void deleteIdea(
        @RequestHeader("authorization") String userId,
        @PathVariable String id
    ) {
        Idea ideaToDelete = database
            .findIdea(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Idea " + id + " does not exist."
                    )
            );
        User user = database
            .findUser(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.FORBIDDEN)
            );
        if (!user.isAdmin() && !ideaToDelete.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteIdea(id, userId);
    }

    @DeleteMapping("/api/ideas/{ideaId}/comments/{commentId}")
    public void deleteComment(
        @RequestHeader("authorization") String userId,
        @PathVariable String ideaId,
        @PathVariable String commentId
    ) {
        Comment commentToDelete = database
            .findCommentOnIdea(ideaId, commentId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comment " + commentId + " does not exist."
                    )
            );
        User user = database
            .findUser(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.FORBIDDEN)
            );
        if (!user.isAdmin() && !commentToDelete.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteComment(commentId, ideaId);
    }
}
