package com.herokuapp.projectideas.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.View;
import com.herokuapp.projectideas.database.documents.Comment;
import com.herokuapp.projectideas.database.documents.Idea;
import com.herokuapp.projectideas.database.documents.User;

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
    public Optional<Idea> getIdea(@PathVariable String id) {
        return database.findIdea(id);
    }

    @GetMapping("/api/ideas/{ideaId}/comments")
    @JsonView(View.Get.class)
    public List<Comment> getCommentsOnIdea(@PathVariable String ideaId) {
        return database.findAllCommentsOnIdea(ideaId);
    }

    @PostMapping("/api/ideas")
    public void createIdea(@RequestHeader("authorization") String userId, @RequestBody @JsonView(View.Post.class) Idea idea) {
        // TODO: Use username from database, not DTO
        Optional<User> user = database.findUser(userId);
        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.createIdea(new Idea(userId, user.get().getUsername(), idea.getTitle(), idea.getContent()));
    }

    @PostMapping("/api/ideas/{ideaId}/comments")
    public void createComment(@RequestHeader("authorization") String userId, @PathVariable String ideaId, @RequestBody @JsonView(View.Post.class) Comment comment) {
        Optional<User> user = database.findUser(userId);
        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.createComment(new Comment(ideaId, user.get().getId(), user.get().getUsername(), comment.getContent()));
    }

    @PutMapping("/api/ideas/{id}")
    public void updateIdea(@RequestHeader("authorization") String userId, @PathVariable String id, @RequestBody @JsonView(View.Post.class) Idea idea) {
        Optional<Idea> existingIdea = database.findIdea(id);
        Optional<User> user = database.findUser(userId);
        if (!user.isPresent() || (!user.get().isAdmin() && !existingIdea.get().getAuthorId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        existingIdea.get().setTitle(idea.getTitle());
        existingIdea.get().setContent(idea.getContent());
        database.updateIdea(id, existingIdea.get());
    }

    @PutMapping("/api/ideas/{ideaId}/comments/{commentId}")
    public void updateComment(@RequestHeader("authorization") String userId, @PathVariable String ideaId, @PathVariable String commentId, @RequestBody @JsonView(View.Post.class) Comment comment) {
        Optional<Comment> existingComment = database.findCommentOnIdea(ideaId, commentId);
        Optional<User> user = database.findUser(userId);
        if (!user.isPresent() || !existingComment.isPresent() || !existingComment.get().getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        existingComment.get().setContent(comment.getContent());
        database.updateComment(existingComment.get());
    }

    @DeleteMapping("/api/ideas/{id}")
    public void deleteIdea(@RequestHeader("authorization") String userId, @PathVariable String id) {
        Optional<Idea> ideaToDelete = database.findIdea(id);
        Optional<User> user = database.findUser(userId);
        if(!ideaToDelete.isPresent()) { return; }
        if(!user.isPresent() || (!user.get().isAdmin() && !ideaToDelete.get().getAuthorId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteIdea(id);
    }

    @DeleteMapping("/api/ideas/{ideaId}/comments/{commentId}")
    public void deleteComment(@RequestHeader("authorization") String userId, @PathVariable String ideaId, @PathVariable String commentId) {
        Optional<Comment> commentToDelete = database.findCommentOnIdea(ideaId, commentId);
        Optional<User> user = database.findUser(userId);
        if (!commentToDelete.isPresent()) {
            return;
        }
        if (!user.isPresent() || !commentToDelete.isPresent() || (!user.get().isAdmin() && !commentToDelete.get().getAuthorId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        database.deleteComment(commentId, ideaId);
    }
}
