package com.herokuapp.projectideas.dto.project;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class ViewProjectAsTeamMemberDTO extends ViewProjectDTO {

    private List<ViewProjectJoinRequestDTO> joinRequests;
    private String inviteId;
}
