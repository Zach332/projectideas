package com.herokuapp.projectideas.database.document.project;

import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ProjectJoinRequest extends UsernameIdPair {

    protected String requestMessage;

    public ProjectJoinRequest(
        String userId,
        String username,
        String requestMessage
    ) {
        super(userId, username);
        this.requestMessage = requestMessage;
    }

    public ProjectJoinRequest(User user, String requestMessage) {
        super(user);
        this.requestMessage = requestMessage;
    }
}
