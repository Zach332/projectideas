package com.herokuapp.projectideas.dto.project;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class CreateProjectDTO {

    private String name;
    private String description;
    private Boolean lookingForMembers;
}
