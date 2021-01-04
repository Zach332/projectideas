package com.herokuapp.projectideas.dto.project;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewProjectDTO extends PreviewProjectDTO {

    private String githubLink;
    private List<String> teamMemberUsernames;
}
