package com.herokuapp.projectideas.dto.post;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewIdeaDTO extends BasePostDTO {

    private String title;
    private Boolean savedByUser;
    private List<String> tags;
}
