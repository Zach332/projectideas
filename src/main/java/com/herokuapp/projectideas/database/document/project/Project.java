package com.herokuapp.projectideas.database.document.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.Authorization;
import com.herokuapp.projectideas.database.document.RootDocument;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import com.herokuapp.projectideas.database.document.vote.Votable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Project implements RootDocument, Votable, Authorization {

    protected String id;
    protected String type;
    protected String projectId;
    protected long timeCreated;
    protected long timeLastEdited;
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
    protected int upvoteCount;

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
        long now = Instant.now().getEpochSecond();
        this.timeCreated = now;
        this.timeLastEdited = now;
        this.ideaId = ideaId;
        this.name = name;
        this.description = description;
        this.teamMembers = new ArrayList<>();
        this.teamMembers.add(initialUser);
        this.publicProject = publicProject;
        this.lookingForMembers = lookingForMembers;
        this.usersRequestingToJoin = new ArrayList<>();
        this.tags = tags;
        this.upvoteCount = 0;
    }

    public String getPartitionKey() {
        return projectId;
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

    public void addUpvote() {
        upvoteCount += 1;
    }

    public void removeUpvote() {
        upvoteCount -= 1;
    }

    public boolean userHasUpvoted(String userId, Database database) {
        return database.userHasUpvotedProject(projectId, userId);
    }

    public boolean userIsAuthorizedToEdit(String userId) {
        return userIsTeamMember(userId);
    }
}
