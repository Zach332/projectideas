package com.herokuapp.projectideas.database.document.project;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Project {

    protected String name;
    protected String description;
    protected String associatedIdeaId;
    protected String githubLink;
    protected List<String> teamMemberIds;

    public Project(
        String name,
        String description,
        String associatedIdeaId,
        String initialTeamMemberId
    ) {
        this.name = name;
        this.description = description;
        this.associatedIdeaId = associatedIdeaId;
        this.teamMemberIds = new ArrayList<>();
        this.teamMemberIds.add(initialTeamMemberId);
    }
}
