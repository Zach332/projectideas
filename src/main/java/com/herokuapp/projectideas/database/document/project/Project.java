package com.herokuapp.projectideas.database.document.project;

import com.herokuapp.projectideas.database.document.user.UserIdPair;
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
    protected List<UserIdPair> teamMembers;
    protected boolean lookingForMembers;
    protected List<UserIdPair> usersRequestingToJoin;

    public Project(
        String name,
        String description,
        String ideaId,
        UserIdPair initialUser,
        boolean lookingForMembers
    ) {
        this.id = UUID.randomUUID().toString();
        this.type = "Project";
        this.projectId = this.id;
        this.ideaId = ideaId;
        this.name = name;
        this.description = description;
        this.teamMembers = new ArrayList<>();
        this.teamMembers.add(initialUser);
        this.lookingForMembers = lookingForMembers;
        this.usersRequestingToJoin = new ArrayList<>();
    }
}
