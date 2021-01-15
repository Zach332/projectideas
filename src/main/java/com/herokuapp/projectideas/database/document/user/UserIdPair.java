package com.herokuapp.projectideas.database.document.user;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class UserIdPair {

    protected String userId;
    protected String username;

    public UserIdPair(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public UserIdPair(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
    }
}
