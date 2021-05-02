package com.herokuapp.projectideas.dto.user;

import com.herokuapp.projectideas.database.document.user.NotificationPreference;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class UpdateUserDTO extends CreateUserDTO {

    private NotificationPreference notificationPreference;
}
