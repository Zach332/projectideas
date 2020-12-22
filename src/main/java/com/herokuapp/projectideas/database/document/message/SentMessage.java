package com.herokuapp.projectideas.database.document.message;

import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class SentMessage {

    protected String id;
    protected String type;
    /**
     * Id of the user that sent the message
     */
    protected String userId;
    protected String recipientUsername;
    protected String content;
    protected long timeSent;

    public SentMessage(
        String senderId,
        String recipientUsername,
        String content
    ) {
        this.id = UUID.randomUUID().toString();
        this.type = "SentMessage";
        this.userId = senderId;
        this.recipientUsername = recipientUsername;
        this.content = content;
        this.timeSent = Instant.now().getEpochSecond();
    }
}
