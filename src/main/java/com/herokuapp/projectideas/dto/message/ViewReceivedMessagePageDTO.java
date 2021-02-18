package com.herokuapp.projectideas.dto.message;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ViewReceivedMessagePageDTO {

    private List<ViewReceivedMessageDTO> receivedMessages;
    private boolean isLastPage;
}
