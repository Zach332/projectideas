package com.herokuapp.projectideas.database.document.message;

import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ReceivedMessage {

    protected String id;
    protected String type;
    /**
     * Id of the user that received the message
     */
    protected String userId;
    protected String senderUsername;
    protected String content;
    protected long timeSent;
    protected boolean unread;

    public ReceivedMessage(
        String recipientId,
        String senderUsername,
        String content
    ) {
        this.id = UUID.randomUUID().toString();
        this.type = "ReceivedMessage";
        this.userId = recipientId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.timeSent = Instant.now().getEpochSecond();
        this.unread = true;
    }
}
