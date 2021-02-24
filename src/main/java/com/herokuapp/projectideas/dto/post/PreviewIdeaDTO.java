package com.herokuapp.projectideas.dto.post;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class PreviewIdeaDTO extends BasePostDTO {

    private String title;
    private int upvoteCount;
}
