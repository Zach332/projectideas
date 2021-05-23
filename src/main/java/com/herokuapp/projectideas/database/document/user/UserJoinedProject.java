package com.herokuapp.projectideas.database.document.user;

import com.herokuapp.projectideas.database.document.RootDocument;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class UserJoinedProject implements RootDocument {

    protected String id;
    protected String type;
    protected String userId;
    protected String projectId;
    protected long timeJoined;

    public UserJoinedProject(String userId, String projectId) {
        this.id = UUID.randomUUID().toString();
        this.type = "UserJoinedProject";
        this.userId = userId;
        this.projectId = projectId;
        this.timeJoined = Instant.now().getEpochSecond();
    }

    public String getPartitionKey() {
        return userId;
    }
}
