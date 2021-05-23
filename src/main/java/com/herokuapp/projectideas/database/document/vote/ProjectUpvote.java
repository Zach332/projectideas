package com.herokuapp.projectideas.database.document.vote;

import com.herokuapp.projectideas.database.document.project.Project;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ProjectUpvote extends Upvote<Project> {

    protected String projectId;

    public ProjectUpvote(String projectId, String userId) {
        super(userId);
        this.type = "ProjectUpvote";
        this.projectId = projectId;
    }

    public String getPartitionKey() {
        return projectId;
    }
}
