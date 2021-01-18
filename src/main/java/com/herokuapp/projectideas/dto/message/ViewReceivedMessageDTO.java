package com.herokuapp.projectideas.dto.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public abstract class ViewReceivedMessageDTO extends ViewMessageDTO {

    private String senderUsername;
    private boolean unread;
}
