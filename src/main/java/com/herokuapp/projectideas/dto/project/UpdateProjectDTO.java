package com.herokuapp.projectideas.dto.project;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class UpdateProjectDTO extends CreateProjectDTO {

    private long timeOfProjectReceipt;
}
