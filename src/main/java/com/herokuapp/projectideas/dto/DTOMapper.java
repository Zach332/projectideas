package com.herokuapp.projectideas.dto;

import com.herokuapp.projectideas.database.document.message.ReceivedMessage;
import com.herokuapp.projectideas.database.document.message.SentMessage;
import com.herokuapp.projectideas.database.document.post.Comment;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.dto.message.ViewReceivedMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentMessageDTO;
import com.herokuapp.projectideas.dto.post.PreviewIdeaDTO;
import com.herokuapp.projectideas.dto.post.ViewCommentDTO;
import com.herokuapp.projectideas.dto.post.ViewIdeaDTO;
import com.herokuapp.projectideas.dto.project.CreateProjectDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectDTO;
import com.herokuapp.projectideas.dto.project.ViewProjectDTO;
import com.herokuapp.projectideas.dto.user.ViewUserDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DTOMapper {
    // Document -> DTO

    ViewUserDTO viewUserDTO(User user);

    PreviewIdeaDTO previewIdeaDTO(Idea idea);
    ViewIdeaDTO viewIdeaDTO(Idea idea, Boolean savedByUser);

    ViewCommentDTO viewCommentDTO(Comment comment);

    ViewReceivedMessageDTO viewReceivedMessageDTO(ReceivedMessage message);
    ViewSentMessageDTO viewSentMessageDTO(SentMessage message);

    PreviewProjectDTO previewProjectDTO(Project project);
    ViewProjectDTO viewProjectDTO(
        Project project,
        List<String> teamMemberUsernames
    );

    // DTO updating existing document

    Project updateProjectFromDTO(
        @MappingTarget Project project,
        CreateProjectDTO createProjectDTO
    );
}
