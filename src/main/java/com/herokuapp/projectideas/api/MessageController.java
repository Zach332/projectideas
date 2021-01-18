package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.message.SendMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewReceivedMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentMessageDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @Autowired
    Database database;

    @Autowired
    DTOMapper mapper;

    @GetMapping("/api/messages/received")
    public List<ViewReceivedMessageDTO> getReceivedMessages(
        @RequestHeader("authorization") String userId
    ) {
        return database
            .findAllReceivedMessages(userId)
            .stream()
            .map(message -> mapper.viewReceivedMessageDTO(message))
            .collect(Collectors.toList());
    }

    @GetMapping("/api/messages/sent")
    public List<ViewSentMessageDTO> getSentMessages(
        @RequestHeader("authorization") String userId
    ) {
        return database
            .findAllSentMessages(userId)
            .stream()
            .map(message -> mapper.viewSentMessageDTO(message))
            .collect(Collectors.toList());
    }

    @GetMapping("/api/messages/numunread")
    public int getNumberOfUnreadMessages(
        @RequestHeader("authorization") String userId
    ) {
        return database.getNumberOfUnreadMessages(userId);
    }

    @PostMapping("/api/messages/{recipientUsername}")
    public void sendIndividualMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable("recipientUsername") String recipientUsername,
        @RequestBody SendMessageDTO message
    ) {
        database.sendIndividualMessage(
            userId,
            recipientUsername,
            message.getContent()
        );
    }

    @PostMapping("/api/messages/projects/{recipientProjectId}")
    public void sendGroupMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable("recipientProjectId") String recipientProjectId,
        @RequestBody SendMessageDTO message
    ) {
        database.sendGroupMessage(
            userId,
            recipientProjectId,
            message.getContent()
        );
    }

    @PostMapping("/api/messages/received/{messageId}/markasread")
    public void markMessageAsRead(
        @RequestHeader("authorization") String userId,
        @PathVariable String messageId
    ) {
        database.markReceivedMessageAsRead(messageId, userId);
    }

    @PostMapping("/api/messages/received/{messageId}/markasunread")
    public void markMessageAsUnread(
        @RequestHeader("authorization") String userId,
        @PathVariable String messageId
    ) {
        database.markReceivedMessageAsUnread(messageId, userId);
    }

    @PostMapping("/api/messages/received/markallasread")
    public void markAllMessagesAsRead(
        @RequestHeader("authorization") String userId
    ) {
        database.markAllReceivedMessagesAsRead(userId);
    }

    @DeleteMapping("/api/messages/received/{messageId}")
    public void deleteReceivedMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable String messageId
    ) {
        database.deleteReceivedMessage(messageId, userId);
    }

    @DeleteMapping("/api/messages/sent/{messageId}")
    public void deleteSentMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable String messageId
    ) {
        database.deleteSentMessage(messageId, userId);
    }
}
