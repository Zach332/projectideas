package com.herokuapp.projectideas.dto.project;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PreviewProjectPageDTO {

    private List<PreviewProjectDTO> projectPreviews;
    private boolean isLastPage;
}
