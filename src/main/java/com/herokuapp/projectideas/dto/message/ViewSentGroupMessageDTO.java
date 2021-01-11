package com.herokuapp.projectideas.dto.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewSentGroupMessageDTO extends ViewSentMessageDTO {

    private String recipientProjectId;
    private String recipientProjectName;
}
