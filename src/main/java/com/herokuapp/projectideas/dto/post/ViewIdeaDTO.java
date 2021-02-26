package com.herokuapp.projectideas.dto.post;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewIdeaDTO extends PreviewIdeaDTO {

    private boolean deleted;
    private Boolean savedByUser;
    private List<String> tags;
}
