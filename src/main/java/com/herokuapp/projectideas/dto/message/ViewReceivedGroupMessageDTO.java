package com.herokuapp.projectideas.dto.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewReceivedGroupMessageDTO extends ViewReceivedMessageDTO {

    private String recipientProjectId;
    private String recipientProjectName;
}
