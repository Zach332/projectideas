package com.herokuapp.projectideas.dto.post;

import com.herokuapp.projectideas.database.document.tag.Tag;
import java.util.List;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewIdeaDTO extends BasePostDTO {

    private String title;
    private Boolean savedByUser;
    private List<Tag> tags;
}
