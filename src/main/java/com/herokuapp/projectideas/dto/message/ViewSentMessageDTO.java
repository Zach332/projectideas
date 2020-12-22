package com.herokuapp.projectideas.dto.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewSentMessageDTO {

    private String id;
    private String recipientUsername;
    private String content;
    private long timeSent;
}
