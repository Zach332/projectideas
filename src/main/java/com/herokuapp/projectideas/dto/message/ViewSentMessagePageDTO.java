package com.herokuapp.projectideas.dto.message;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ViewSentMessagePageDTO {

    private List<ViewSentMessageDTO> sentMessages;
    private boolean isLastPage;
}
