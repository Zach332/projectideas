package com.herokuapp.projectideas.dto.project;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class PreviewProjectDTO {

    private String id;
    private String name;
    private String description;
    private boolean lookingForMembers;
    private boolean userIsTeamMember;
    private boolean userHasRequestedToJoin;
    private int upvoteCount;
    private boolean userHasUpvoted;
}
