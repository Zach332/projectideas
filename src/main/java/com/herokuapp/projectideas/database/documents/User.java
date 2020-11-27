package com.herokuapp.projectideas.database.documents;

import java.time.Instant;
import java.util.UUID;

import lombok.*;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
    final private String id = UUID.randomUUID().toString();
    @NonNull private String username;
    @NonNull private String email;
    final private long timeCreated = Instant.now().getEpochSecond();
}
