package com.herokuapp.projectideas.database.document.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class SentIndividualMessage extends SentMessage {

    protected String recipientUsername;

    public SentIndividualMessage(
        String senderId,
        String recipientUsername,
        String content
    ) {
        super(senderId, content);
        this.type = "SentIndividualMessage";
        this.recipientUsername = recipientUsername;
    }
}
