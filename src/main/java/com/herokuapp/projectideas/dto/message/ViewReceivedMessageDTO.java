package com.herokuapp.projectideas.dto.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewReceivedMessageDTO {

    private String id;
    private String senderUsername;
    private String content;
    private long timeSent;
    private boolean unread;
}
