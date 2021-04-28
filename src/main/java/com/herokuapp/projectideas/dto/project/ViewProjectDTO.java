package com.herokuapp.projectideas.dto.project;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewProjectDTO extends PreviewProjectDTO {

    private String githubLink;
    private List<String> teamMemberUsernames;
    private boolean publicProject;
    private boolean lookingForMembers;
    private List<String> tags;
}
