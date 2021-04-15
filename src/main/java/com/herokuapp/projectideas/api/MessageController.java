package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.exception.EmptyPointReadException;
import com.herokuapp.projectideas.database.exception.EmptySingleDocumentQueryException;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.message.SendMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewReceivedMessagePageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentMessagePageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    ) {
        try {
            return database.getNumberOfUnreadMessages(userId);
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/api/messages/{recipientUsername}")
    public void sendIndividualMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable("recipientUsername") String recipientUsername,
        @RequestBody SendMessageDTO message
    ) {
        try {
            database.sendIndividualMessage(
                userId,
                recipientUsername,
                message.getContent()
            );
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (EmptySingleDocumentQueryException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User " + recipientUsername + " does not exist."
            );
        }
    }

    @PostMapping("/api/messages/projects/{recipientProjectId}")
    public void sendGroupMessage(
        @RequestHeader("authorization") String userId,
        @PathVariable("recipientProjectId") String recipientProjectId,
        @RequestBody SendMessageDTO message
    ) {
        try {
            database.sendGroupMessage(
                userId,
                recipientProjectId,
                message.getContent()
            );
        } catch (EmptyPointReadException e) {
            if (e.getDocumentType().equals("User")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            } else {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Project " + recipientProjectId + " does not exist."
                );
            }
        }
    }

    @PostMapping("/api/messages/received/markallasread")
    public void markAllMessagesAsRead(
        @RequestHeader("authorization") String userId
    ) {
        try {
            database.markAllReceivedMessagesAsRead(userId);
        } catch (EmptyPointReadException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
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
