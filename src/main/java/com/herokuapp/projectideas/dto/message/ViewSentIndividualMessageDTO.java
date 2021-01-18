package com.herokuapp.projectideas.dto.message;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewSentIndividualMessageDTO extends ViewSentMessageDTO {

    private String recipientUsername;
}
