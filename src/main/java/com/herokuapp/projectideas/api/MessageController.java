package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.exception.DatabaseException;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.message.SendMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewReceivedMessagePageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentMessagePageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @Autowired
    Database database;

    @Autowired
    DTOMapper mapper;

    @GetMapping("/api/messages/received")
    public ViewReceivedMessagePageDTO getReceivedMessages(
        @RequestHeader("authorization") String userId,
        @RequestParam("page") int pageNum
    ) {
        return mapper.viewReceivedMessagePageDTO(
            database.getReceivedMessagesByPage(userId, pageNum)
        );
    }

    @GetMapping("/api/messages/sent")
    public ViewSentMessagePageDTO getSentMessages(
        @RequestHeader("authorization") String userId,
        @RequestParam("page") int pageNum
    ) {
        return mapper.viewSentMessagePageDTO(
            database.getSentMessagesByPage(userId, pageNum)
        );
    }

    @GetMapping("/api/messages/numunread")
    public int getNumberOfUnreadMessages(
        @RequestHeader("authorization") String userId
    ) throws DatabaseException {
        return database.getNumberOfUnreadMessages(userId);
    }

    @PostMapping("/api/messages/{recipientUsername}")
    public void sendIndividualMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable("recipientUsername") String recipientUsername,
        @RequestBody SendMessageDTO message
    ) throws DatabaseException {
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
    ) throws DatabaseException {
        database.sendGroupMessage(
            userId,
            recipientProjectId,
            message.getContent()
        );
    }

    @PostMapping("/api/messages/received/markallasread")
    public void markAllMessagesAsRead(
        @RequestHeader("authorization") String userId
    ) throws DatabaseException {
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
