package com.herokuapp.projectideas.database.document.project;

import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
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
    protected List<UsernameIdPair> teamMembers;
    protected boolean lookingForMembers;
    protected List<UsernameIdPair> usersRequestingToJoin;

    public Project(
        String name,
        String description,
        String ideaId,
        UsernameIdPair initialUser,
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
