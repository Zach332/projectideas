package com.herokuapp.projectideas.dto.post;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class PostIdeaDTO {

    private String content;
    private String title;
    private List<String> tags;
}
