package com.herokuapp.projectideas.database.document.message;

import com.fasterxml.jackson.annotation.JsonView;
import com.herokuapp.projectideas.database.View;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class SentMessage {

    @JsonView(View.Get.class)
    protected String id;

    protected String type;

    /**
     * Id of the user that sent the message
     */
    protected String userId;

    @JsonView(View.Get.class)
    protected String recipientUsername;

    @JsonView(View.Base.class)
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
