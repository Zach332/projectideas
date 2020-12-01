package com.herokuapp.projectideas.api;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.View;
import com.herokuapp.projectideas.database.document.Comment;
import com.herokuapp.projectideas.database.document.Idea;
import com.herokuapp.projectideas.database.document.User;

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
public class IdeaController {
    
    @Autowired
    Database database;

    @GetMapping("/api/ideas")
    @JsonView(View.Get.class)
    public List<Idea> getAllIdeas() {
        List<Idea> allIdeas =  database.findAllIdeas();
        Collections.reverse(allIdeas);
        return allIdeas;
    }

    @GetMapping("/api/ideas/{id}")
    @JsonView(View.Get.class)
    public Idea getIdea(@PathVariable String id) {
        return database.findIdea(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea " + id + " does not exist."));
    }

    @GetMapping("/api/ideas/{ideaId}/comments")
    @JsonView(View.Get.class)
    public List<Comment> getCommentsOnIdea(@PathVariable String ideaId) {
        return database.findAllCommentsOnIdea(ideaId);
    }

    @PostMapping("/api/ideas")
    public void createIdea(@RequestHeader("authorization") String userId, @RequestBody @JsonView(View.Post.class) Idea idea) {
        User user = database.findUser(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        database.createIdea(new Idea(userId, user.getUsername(), idea.getTitle(), idea.getContent()));
    }

    @PostMapping("/api/ideas/{ideaId}/comments")
    public void createComment(@RequestHeader("authorization") String userId, @PathVariable String ideaId, @RequestBody @JsonView(View.Post.class) Comment comment) {
        User user = database.findUser(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        database.createComment(new Comment(ideaId, user.getId(), user.getUsername(), comment.getContent()));
    }

    @PutMapping("/api/ideas/{id}")
    public void updateIdea(@RequestHeader("authorization") String userId, @PathVariable String id, @RequestBody @JsonView(View.Post.class) Idea idea) {
        Idea existingIdea = database.findIdea(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea " + id + " does not exist."));
        User user = database.findUser(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if (!user.isAdmin() && !existingIdea.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        existingIdea.setTitle(idea.getTitle());
        existingIdea.setContent(idea.getContent());
        database.updateIdea(id, existingIdea);
    }

    @PutMapping("/api/ideas/{ideaId}/comments/{commentId}")
    public void updateComment(@RequestHeader("authorization") String userId, @PathVariable String ideaId, @PathVariable String commentId, @RequestBody @JsonView(View.Post.class) Comment comment) {
        Comment existingComment = database.findCommentOnIdea(ideaId, commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment " + commentId + " does not exist."));
        if (!existingComment.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        existingComment.setContent(comment.getContent());
        database.updateComment(existingComment);
    }

    @DeleteMapping("/api/ideas/{id}")
    public void deleteIdea(@RequestHeader("authorization") String userId, @PathVariable String id) {
        Idea ideaToDelete = database.findIdea(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea " + id + " does not exist."));
        User user = database.findUser(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if(!user.isAdmin() && !ideaToDelete.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteIdea(id);
    }

    @DeleteMapping("/api/ideas/{ideaId}/comments/{commentId}")
    public void deleteComment(@RequestHeader("authorization") String userId, @PathVariable String ideaId, @PathVariable String commentId) {
        Comment commentToDelete = database.findCommentOnIdea(ideaId, commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment " + commentId + " does not exist."));
        User user = database.findUser(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if (!user.isAdmin() && !commentToDelete.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteComment(commentId, ideaId);
    }
}
