package com.herokuapp.projectideas.api;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.View;
import com.herokuapp.projectideas.database.document.message.ReceivedMessage;
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
    public List<ReceivedMessage> getReceivedMessages(
        @RequestHeader("authorization") String userId
    ) {
        return database.findAllReceivedMessages(userId);
    }

    // TODO: Refactor to not rely on the ReceivedMessage type
    @PostMapping("/api/messages/{recipientUsername}")
    public void sendMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable("recipientUsername") String recipientUsername,
        @RequestBody @JsonView(View.Post.class) ReceivedMessage message
    ) {
        database.createMessage(userId, recipientUsername, message.getContent());
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
        ReceivedMessage existingMessage = database
            .findReceivedMessage(recipientId, messageId)
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
        database.updateReceivedMessage(existingMessage);
    }

    @DeleteMapping("/api/messages/{messageId}")
    public void deleteMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable String messageId
    ) {
        database.deleteReceivedMessage(messageId, userId);
    }
}
