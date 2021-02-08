package com.herokuapp.projectideas.dto;

import com.herokuapp.projectideas.database.document.DocumentPage;
import com.herokuapp.projectideas.database.document.message.ReceivedGroupMessage;
import com.herokuapp.projectideas.database.document.message.ReceivedIndividualMessage;
import com.herokuapp.projectideas.database.document.message.ReceivedMessage;
import com.herokuapp.projectideas.database.document.message.SentGroupMessage;
import com.herokuapp.projectideas.database.document.message.SentIndividualMessage;
import com.herokuapp.projectideas.database.document.message.SentMessage;
import com.herokuapp.projectideas.database.document.post.Comment;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.project.ProjectJoinRequest;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.dto.message.ViewReceivedGroupMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewReceivedIndividualMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewReceivedMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentGroupMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentIndividualMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentMessageDTO;
import com.herokuapp.projectideas.dto.post.PostCommentDTO;
import com.herokuapp.projectideas.dto.post.PostIdeaDTO;
import com.herokuapp.projectideas.dto.post.PreviewIdeaDTO;
import com.herokuapp.projectideas.dto.post.PreviewIdeaPageDTO;
import com.herokuapp.projectideas.dto.post.ViewCommentDTO;
import com.herokuapp.projectideas.dto.post.ViewIdeaDTO;
import com.herokuapp.projectideas.dto.project.CreateProjectDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectPageDTO;
import com.herokuapp.projectideas.dto.project.ViewProjectAsTeamMemberDTO;
import com.herokuapp.projectideas.dto.project.ViewProjectDTO;
import com.herokuapp.projectideas.dto.project.ViewProjectJoinRequestDTO;
import com.herokuapp.projectideas.dto.user.CreateUserDTO;
import com.herokuapp.projectideas.dto.user.ViewUserDTO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public abstract class DTOMapper {

    // Document -> DTO

    public abstract ViewUserDTO viewUserDTO(User user);

    public abstract PreviewIdeaDTO previewIdeaDTO(Idea idea);

    public abstract ViewIdeaDTO viewIdeaDTO(Idea idea, Boolean savedByUser);

    @Mapping(target = "ideaPreviews", source = "documents")
    public abstract PreviewIdeaPageDTO previewIdeaPageDTO(
        DocumentPage<Idea> documentPage
    );

    public abstract ViewCommentDTO viewCommentDTO(Comment comment);

    public abstract ViewReceivedIndividualMessageDTO viewReceivedIndividualMessageDTO(
        ReceivedIndividualMessage message,
        boolean groupMessage
    );

    public abstract ViewReceivedGroupMessageDTO viewReceivedGroupMessageDTO(
        ReceivedGroupMessage message,
        boolean groupMessage
    );

    public abstract ViewSentIndividualMessageDTO viewSentIndividualMessageDTO(
        SentIndividualMessage message,
        boolean groupMessage
    );

    public abstract ViewSentGroupMessageDTO viewSentGroupMessageDTO(
        SentGroupMessage message,
        boolean groupMessage
    );

    public ViewReceivedMessageDTO viewReceivedMessageDTO(
        ReceivedMessage message
    ) {
        if (message instanceof ReceivedIndividualMessage) {
            return viewReceivedIndividualMessageDTO(
                (ReceivedIndividualMessage) message,
                false
            );
        } else if (message instanceof ReceivedGroupMessage) {
            return viewReceivedGroupMessageDTO(
                (ReceivedGroupMessage) message,
                true
            );
        }
        return null;
    }

    public ViewSentMessageDTO viewSentMessageDTO(SentMessage message) {
        if (message instanceof SentIndividualMessage) {
            return viewSentIndividualMessageDTO(
                (SentIndividualMessage) message,
                false
            );
        } else if (message instanceof SentGroupMessage) {
            return viewSentGroupMessageDTO((SentGroupMessage) message, true);
        }
        return null;
    }

    @Mapping(
        target = "userIsTeamMember",
        source = "project",
        qualifiedByName = "userIsTeamMember"
    )
    @Mapping(
        target = "userHasRequestedToJoin",
        source = "project",
        qualifiedByName = "userHasRequestedToJoin"
    )
    public abstract PreviewProjectDTO previewProjectDTO(
        Project project,
        @Context String userId
    );

    @Mapping(
        target = "userIsTeamMember",
        source = "project",
        qualifiedByName = "userIsTeamMember"
    )
    @Mapping(
        target = "userHasRequestedToJoin",
        source = "project",
        qualifiedByName = "userHasRequestedToJoin"
    )
    public abstract ViewProjectDTO viewProjectDTO(
        Project project,
        @Context String userId
    );

    @Mapping(
        target = "userIsTeamMember",
        source = "project",
        qualifiedByName = "userIsTeamMember"
    )
    @Mapping(
        target = "userHasRequestedToJoin",
        source = "project",
        qualifiedByName = "userHasRequestedToJoin"
    )
    @Mapping(target = "joinRequests", source = "usersRequestingToJoin")
    public abstract ViewProjectAsTeamMemberDTO viewProjectAsTeamMemberDTO(
        Project project,
        @Context String userId
    );

    @Mapping(target = "projectPreviews", source = "documents")
    public abstract PreviewProjectPageDTO previewProjectPageDTO(
        DocumentPage<Project> documentPage
    );

    @Named("userIsTeamMember")
    protected boolean userIsTeamMember(
        Project project,
        @Context String userId
    ) {
        return project.userIsTeamMember(userId);
    }

    @Named("userHasRequestedToJoin")
    protected boolean userHasRequestedToJoin(
        Project project,
        @Context String userId
    ) {
        return project.userHasRequestedToJoin(userId);
    }

    protected abstract ViewProjectJoinRequestDTO viewProjectJoinRequest(
        ProjectJoinRequest projectJoinRequest
    );

    // DTO updating existing document

    public abstract User updateUserFromDTO(
        @MappingTarget User user,
        CreateUserDTO createUserDTO
    );

    public abstract void updateIdeaFromDTO(
        @MappingTarget Idea idea,
        PostIdeaDTO postIdeaDTO
    );

    public abstract void updateCommentFromDTO(
        @MappingTarget Comment comment,
        PostCommentDTO postCommentDTO
    );

    public abstract void updateProjectFromDTO(
        @MappingTarget Project project,
        CreateProjectDTO createProjectDTO
    );
}
