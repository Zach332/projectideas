package com.herokuapp.projectideas.database.document.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ReceivedIndividualMessage extends ReceivedMessage {

    public ReceivedIndividualMessage(
        String recipientId,
        String senderUsername,
        String content
    ) {
        super(recipientId, senderUsername, content);
        this.type = "ReceivedIndividualMessage";
    }
}
