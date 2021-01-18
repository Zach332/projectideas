package com.herokuapp.projectideas.dto.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public abstract class ViewMessageDTO {

    private String id;
    private String content;
    private boolean groupMessage;
    private long timeSent;
}
