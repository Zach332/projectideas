package com.herokuapp.projectideas.dto;

import com.herokuapp.projectideas.database.document.User;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.dto.post.PreviewIdeaDTO;
import com.herokuapp.projectideas.dto.user.ViewUserDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DTOMapper {
    PreviewIdeaDTO previewIdeaDTO(Idea idea);

    ViewUserDTO userDTO(User user);
}
