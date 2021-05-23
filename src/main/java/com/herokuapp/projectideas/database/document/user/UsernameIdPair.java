package com.herokuapp.projectideas.database.document.user;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class UsernameIdPair {

    protected String userId;
    protected String username;

    public UsernameIdPair(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public UsernameIdPair(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
    }
}
