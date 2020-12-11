package com.herokuapp.projectideas.api;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.View;
import com.herokuapp.projectideas.database.document.Message;
import com.herokuapp.projectideas.database.document.User;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MessageController {

    @Autowired
    Database database;

    @GetMapping("/api/messages/received")
    @JsonView(View.Get.class)
    public List<Message> getReceivedMessages(
        @RequestHeader("authorization") String userId
    ) {
        return database.findAllMessagesToUser(userId);
    }

    @GetMapping("/api/messages/numunread")
    public int getNumUnreadMessagesToUser(
        @RequestHeader("authorization") String userId
    ) {
        return database.getNumUnreadMessagesToUser(userId);
    }

    @PostMapping("/api/messages/{recipientUsername}")
    public void sendMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable("recipientUsername") String recipientUsername,
        @RequestBody @JsonView(View.Post.class) Message message
    ) {
        User sender = database
            .findUser(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.FORBIDDEN)
            );
        User recipient = database
            .findUserByUsername(recipientUsername)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User " + recipientUsername + " does not exist."
                    )
            );
        database.createMessage(
            new Message(
                userId,
                sender.getUsername(),
                recipient.getId(),
                message.getContent()
            )
        );
    }

    @PostMapping("/api/messages/{messageId}/markasread")
    public void markMessageAsRead(
        @RequestHeader("authorization") String userId,
        @PathVariable String messageId
    ) {
        markMessage(userId, messageId, false);
    }

    @PostMapping("/api/messages/{messageId}/markasunread")
    public void markMessageAsUnread(
        @RequestHeader("authorization") String userId,
        @PathVariable String messageId
    ) {
        markMessage(userId, messageId, true);
    }

    private void markMessage(
        String recipientId,
        String messageId,
        boolean unread
    ) {
        Message existingMessage = database
            .findMessageToUser(recipientId, messageId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Message " +
                        messageId +
                        " to user " +
                        recipientId +
                        " does not exist."
                    )
            );
        existingMessage.setUnread(unread);
        database.updateMessage(existingMessage);
    }

    @DeleteMapping("/api/messages/{messageId}")
    public void deleteMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable String messageId
    ) {
        database.deleteMessage(messageId, userId);
    }
}
