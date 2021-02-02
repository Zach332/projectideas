package com.herokuapp.projectideas.database.document.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.herokuapp.projectideas.database.document.RootDocument;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Project implements RootDocument {

    protected String id;
    protected String type;
    protected String projectId;
    protected long timeCreated;
    /**
     * Id of the idea associated with this project
     */
    protected String ideaId;
    protected String name;
    protected String description;
    protected String githubLink;
    protected List<UsernameIdPair> teamMembers;
    protected boolean publicProject;
    protected boolean lookingForMembers;
    protected List<ProjectJoinRequest> usersRequestingToJoin;
    protected List<String> tags;

    public Project(
        String name,
        String description,
        String ideaId,
        UsernameIdPair initialUser,
        boolean publicProject,
        boolean lookingForMembers,
        List<String> tags
    ) {
        this.id = UUID.randomUUID().toString();
        this.type = "Project";
        this.projectId = this.id;
        this.timeCreated = Instant.now().getEpochSecond();
        this.ideaId = ideaId;
        this.name = name;
        this.description = description;
        this.teamMembers = new ArrayList<>();
        this.teamMembers.add(initialUser);
        this.publicProject = publicProject;
        this.lookingForMembers = lookingForMembers;
        this.usersRequestingToJoin = new ArrayList<>();
        this.tags = tags;
    }

    @JsonIgnore
    public boolean userIsTeamMember(String userId) {
        return teamMembers
            .stream()
            .anyMatch(
                usernameIdPair -> usernameIdPair.getUserId().equals(userId)
            );
    }

    @JsonIgnore
    public boolean userHasRequestedToJoin(String userId) {
        return usersRequestingToJoin
            .stream()
            .anyMatch(
                projectJoinRequest ->
                    projectJoinRequest.getUserId().equals(userId)
            );
    }

    @JsonIgnore
    public List<String> getTeamMemberUsernames() {
        return teamMembers
            .stream()
            .map(usernameIdPair -> usernameIdPair.getUsername())
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> getJoinRequestUsernames() {
        return usersRequestingToJoin
            .stream()
            .map(projectJoinRequest -> projectJoinRequest.getUsername())
            .collect(Collectors.toList());
    }
}
