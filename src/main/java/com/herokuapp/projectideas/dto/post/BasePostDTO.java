package com.herokuapp.projectideas.dto.post;

import lombok.*;

@Getter
@Setter
abstract class BasePostDTO {

    private String id;
    private long timePosted;
    private long timeLastEdited;
    private String authorUsername;
    private String content;
}
