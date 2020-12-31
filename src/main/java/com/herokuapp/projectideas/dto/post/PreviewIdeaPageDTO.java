package com.herokuapp.projectideas.dto.post;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PreviewIdeaPageDTO {

    private List<PreviewIdeaDTO> ideaPreviews;
    private boolean isLastPage;
}
