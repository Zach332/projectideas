package com.herokuapp.projectideas.database.document.project;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Project {

    protected String id;
    protected String type;
    protected String projectId;
    /**
     * Id of the idea associated with this project
     */
    protected String ideaId;
    protected String name;
    protected String description;
    protected String githubLink;
    protected List<String> teamMemberIds;
    protected Boolean lookingForMembers;

    public Project(
        String name,
        String description,
        String ideaId,
        String initialTeamMemberId,
        Boolean lookingForMembers
    ) {
        this.id = UUID.randomUUID().toString();
        this.type = "Project";
        this.projectId = this.id;
        this.ideaId = ideaId;
        this.name = name;
        this.description = description;
        this.teamMemberIds = new ArrayList<>();
        this.teamMemberIds.add(initialTeamMemberId);
        this.lookingForMembers = lookingForMembers;
    }
}
