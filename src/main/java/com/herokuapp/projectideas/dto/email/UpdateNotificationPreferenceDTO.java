package com.herokuapp.projectideas.dto.email;

import com.herokuapp.projectideas.database.document.user.NotificationPreference;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class UpdateNotificationPreferenceDTO {

    private NotificationPreference notificationPreference;
}
