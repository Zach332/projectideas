package com.herokuapp.projectideas.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.herokuapp.projectideas.database.Database;
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
import com.herokuapp.projectideas.dto.message.ViewReceivedMessagePageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentGroupMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentIndividualMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentMessageDTO;
import com.herokuapp.projectideas.dto.message.ViewSentMessagePageDTO;
import com.herokuapp.projectideas.dto.post.PostCommentDTO;
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
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class DTOMapper {

    @Autowired
    private ObjectMapper objectMapper;

    // Document -> DTO

    public abstract ViewUserDTO viewUserDTO(User user);

    @Mapping(
        target = "userHasUpvoted",
        source = "idea",
        qualifiedByName = "userHasUpvotedIdea"
    )
    @Named("previewIdeaDTO")
    public abstract PreviewIdeaDTO previewIdeaDTO(
        Idea idea,
        @Context String userId,
        @Context Database database
    );

    @Mapping(
        target = "userHasUpvoted",
        source = "idea",
        qualifiedByName = "userHasUpvotedIdea"
    )
    @Mapping(
        target = "savedByUser",
        source = "idea",
        qualifiedByName = "userHasSavedIdea"
    )
    public abstract ViewIdeaDTO viewIdeaDTO(
        Idea idea,
        @Context String userId,
        @Context Database database
    );

    @Mapping(
        target = "ideaPreviews",
        source = "documents",
        qualifiedByName = "previewIdeaDTOList"
    )
    public abstract PreviewIdeaPageDTO previewIdeaPageDTO(
        DocumentPage<Idea> documentPage,
        @Context String userId,
        @Context Database database
    );

    @Named("userHasUpvotedIdea")
    protected boolean userHasUpvotedIdea(
        Idea idea,
        @Context String userId,
        @Context Database database
    ) {
        return idea.userHasUpvoted(userId, database);
    }

    @Named("userHasSavedIdea")
    protected boolean userHasSavedIdea(
        Idea idea,
        @Context String userId,
        @Context Database database
    ) {
        return idea.savedByUser(userId, database);
    }

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

    @Mapping(target = "receivedMessages", source = "documents")
    public abstract ViewReceivedMessagePageDTO viewReceivedMessagePageDTO(
        DocumentPage<ReceivedMessage> documentPage
    );

    @Mapping(target = "sentMessages", source = "documents")
    public abstract ViewSentMessagePageDTO viewSentMessagePageDTO(
        DocumentPage<SentMessage> documentPage
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
    @Mapping(
        target = "userHasUpvoted",
        source = "project",
        qualifiedByName = "userHasUpvotedProject"
    )
    @Named("previewProjectDTO")
    public abstract PreviewProjectDTO previewProjectDTO(
        Project project,
        @Context String userId,
        @Context Database database
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
    @Mapping(
        target = "userHasUpvoted",
        source = "project",
        qualifiedByName = "userHasUpvotedProject"
    )
    @Mapping(
        target = "timeSent",
        expression = "java( java.time.Instant.now().getEpochSecond() )"
    )
    public abstract ViewProjectDTO viewProjectDTO(
        Project project,
        @Context String userId,
        @Context Database database
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
    @Mapping(
        target = "userHasUpvoted",
        source = "project",
        qualifiedByName = "userHasUpvotedProject"
    )
    @Mapping(
        target = "timeSent",
        expression = "java( java.time.Instant.now().getEpochSecond() )"
    )
    @Mapping(target = "joinRequests", source = "usersRequestingToJoin")
    public abstract ViewProjectAsTeamMemberDTO viewProjectAsTeamMemberDTO(
        Project project,
        @Context String userId,
        @Context Database database
    );

    @Mapping(
        target = "projectPreviews",
        source = "documents",
        qualifiedByName = "previewProjectDTOList"
    )
    public abstract PreviewProjectPageDTO previewProjectPageDTO(
        DocumentPage<Project> documentPage,
        @Context String userId,
        @Context Database database
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

    @Named("userHasUpvotedProject")
    protected boolean userHasUpvotedProject(
        Project project,
        @Context String userId,
        @Context Database database
    ) {
        return project.userHasUpvoted(userId, database);
    }

    protected abstract ViewProjectJoinRequestDTO viewProjectJoinRequest(
        ProjectJoinRequest projectJoinRequest
    );

    // Document collection -> DTO collection

    @IterableMapping(qualifiedByName = "previewIdeaDTO")
    @Named("previewIdeaDTOList")
    protected abstract List<PreviewIdeaDTO> previewIdeaDTOList(
        List<Idea> ideas,
        @Context String userId,
        @Context Database database
    );

    @IterableMapping(qualifiedByName = "previewProjectDTO")
    @Named("previewProjectDTOList")
    protected abstract List<PreviewProjectDTO> previewProjectDTOList(
        List<Project> projects,
        @Context String userId,
        @Context Database database
    );

    // DTO updating existing document

    public abstract User updateUserFromDTO(
        @MappingTarget User user,
        CreateUserDTO createUserDTO
    );

    public Idea getIdeaFromPatch(Idea idea, JsonPatch patch)
        throws IllegalArgumentException, JsonPatchException {
        return patchDocument(patch, idea, Idea.class);
    }

    public abstract void updateCommentFromDTO(
        @MappingTarget Comment comment,
        PostCommentDTO postCommentDTO
    );

    public abstract void updateProjectFromDTO(
        @MappingTarget Project project,
        CreateProjectDTO createProjectDTO
    );

    private <T> T patchDocument(
        JsonPatch patch,
        T targetDocument,
        Class<T> targetClass
    ) throws IllegalArgumentException, JsonPatchException {
        JsonNode patchedDocument = patch.apply(
            objectMapper.convertValue(targetDocument, JsonNode.class)
        );
        return objectMapper.convertValue(patchedDocument, targetClass);
    }
}
