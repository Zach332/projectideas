package com.herokuapp.projectideas.dto.user;

import com.herokuapp.projectideas.database.document.user.NotificationPreference;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewUserDTO {

    private String username;
    private String email;
    private long timeCreated;
    private boolean admin;
    private NotificationPreference notificationPreference;
}
