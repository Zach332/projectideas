package com.herokuapp.projectideas.dto;

import com.herokuapp.projectideas.database.document.User;
import com.herokuapp.projectideas.database.document.message.ReceivedMessage;
import com.herokuapp.projectideas.database.document.message.SentMessage;
import com.herokuapp.projectideas.database.document.post.Comment;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.dto.message.ViewReceivedMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentMessageDTO;
import com.herokuapp.projectideas.dto.post.PreviewIdeaDTO;
import com.herokuapp.projectideas.dto.post.ViewCommentDTO;
import com.herokuapp.projectideas.dto.post.ViewIdeaDTO;
import com.herokuapp.projectideas.dto.user.ViewUserDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DTOMapper {
    ViewUserDTO viewUserDTO(User user);

    PreviewIdeaDTO previewIdeaDTO(Idea idea);
    ViewIdeaDTO viewIdeaDTO(Idea idea, Boolean savedByUser);

    ViewCommentDTO viewCommentDTO(Comment comment);

    ViewReceivedMessageDTO viewReceivedMessageDTO(ReceivedMessage message);
    ViewSentMessageDTO viewSentMessageDTO(SentMessage message);
}
