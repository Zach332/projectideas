package com.herokuapp.projectideas.database;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.herokuapp.projectideas.database.document.message.ReceivedGroupMessage;
import com.herokuapp.projectideas.database.document.message.ReceivedIndividualMessage;
import com.herokuapp.projectideas.database.document.message.ReceivedMessage;
import com.herokuapp.projectideas.database.document.message.SentGroupMessage;
import com.herokuapp.projectideas.database.document.message.SentIndividualMessage;
import com.herokuapp.projectideas.database.document.message.SentMessage;
import com.herokuapp.projectideas.database.document.post.Comment;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.user.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Database {

    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer userContainer;
    private CosmosContainer postContainer;
    private CosmosContainer projectContainer;

    public Database(
        @Value("${azure.cosmos.uri}") String uri,
        @Value("${azure.cosmos.key}") String key,
        @Value("${projectideas.collectionPrefix}") String collectionPrefix
    ) {
        client = new CosmosClientBuilder().endpoint(uri).key(key).buildClient();
        database = client.getDatabase("projectideas");
        userContainer = database.getContainer(collectionPrefix + "_users");
        postContainer = database.getContainer(collectionPrefix + "_posts");
        projectContainer =
            database.getContainer(collectionPrefix + "_projects");
    }

    // Users

    public void createUser(User user) {
        userContainer.createItem(user);
    }

    public Optional<User> findUser(String id) {
        return userContainer
            .queryItems(
                "SELECT * FROM c WHERE c.userId = '" + id + "'",
                new CosmosQueryRequestOptions(),
                User.class
            )
            .stream()
            .findFirst();
    }

    public Optional<User> findUserByEmail(String email) {
        return userContainer
            .queryItems(
                "SELECT * FROM c WHERE c.email = '" + email + "'",
                new CosmosQueryRequestOptions(),
                User.class
            )
            .stream()
            .findFirst();
    }

    public Optional<User> findUserByUsername(String username) {
        return userContainer
            .queryItems(
                "SELECT * FROM c WHERE c.username = '" + username + "'",
                new CosmosQueryRequestOptions(),
                User.class
            )
            .stream()
            .findFirst();
    }

    public String getUsernameFromId(String userId) {
        return userContainer
            .queryItems(
                "SELECT VALUE c.username FROM c WHERE c.type = 'User' AND c.userId = '" +
                userId +
                "'",
                new CosmosQueryRequestOptions(),
                String.class
            )
            .stream()
            .findFirst()
            .get();
    }

    public boolean containsUserWithUsername(String username) {
        return (
            userContainer
                .queryItems(
                    "SELECT VALUE COUNT(1) FROM c WHERE c.username = '" +
                    username +
                    "'",
                    new CosmosQueryRequestOptions(),
                    Integer.class
                )
                .stream()
                .findFirst()
                .get() >
            0
        );
    }

    public void updateUser(String id, User user) {
        User oldUser = findUser(id).get();

        // Handle username denormalization
        if (!user.getUsername().equals(oldUser.getUsername())) {
            ArrayList<Object> params = new ArrayList<Object>();
            params.add(user.getId());
            params.add(user.getUsername());

            CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();

            List<PartitionKey> partitionKeys = postContainer
                .queryItems(
                    "SELECT VALUE c.ideaId FROM c WHERE c.authorId = '" +
                    user.getId() +
                    "'",
                    new CosmosQueryRequestOptions(),
                    String.class
                )
                .stream()
                .distinct()
                .map(ideaId -> new PartitionKey(ideaId))
                .collect(Collectors.toList());

            for (PartitionKey partitionKey : partitionKeys) {
                options.setPartitionKey(partitionKey);
                postContainer
                    .getScripts()
                    .getStoredProcedure("updateUsername")
                    .execute(params, options);
            }
        }

        userContainer.replaceItem(
            user,
            id,
            new PartitionKey(id),
            new CosmosItemRequestOptions()
        );
    }

    public void saveIdeaForUser(String ideaId, String userId) {
        User user = findUser(userId).get();
        if (!user.getSavedIdeaIds().contains(ideaId)) {
            user.getSavedIdeaIds().add(ideaId);
        }
        userContainer.replaceItem(
            user,
            userId,
            new PartitionKey(userId),
            new CosmosItemRequestOptions()
        );
    }

    public void unsaveIdeaForUser(String ideaId, String userId) {
        User user = findUser(userId).get();
        user.getSavedIdeaIds().remove(ideaId);
        userContainer.replaceItem(
            user,
            userId,
            new PartitionKey(userId),
            new CosmosItemRequestOptions()
        );
    }

    private List<String> getSavedIdeaIdsForUser(String userId) {
        return userContainer
            .queryItems(
                "SELECT VALUE c.savedIdeaIds FROM c WHERE c.userId = '" +
                userId +
                "'",
                new CosmosQueryRequestOptions(),
                String.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    private List<String> getPostedIdeaIdsForUser(String userId) {
        return userContainer
            .queryItems(
                "SELECT VALUE c.postedIdeaIds FROM c WHERE c.userId = '" +
                userId +
                "'",
                new CosmosQueryRequestOptions(),
                String.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    private List<String> getJoinedProjectIdsForUser(String userId) {
        return userContainer
            .queryItems(
                "SELECT VALUE c.joinedProjectIds FROM c WHERE c.userId = '" +
                userId +
                "'",
                new CosmosQueryRequestOptions(),
                String.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public List<Idea> getSavedIdeasForUser(String userId) {
        ArrayList<Idea> ideas = new ArrayList<Idea>();
        List<String> ideaIds = getSavedIdeaIdsForUser(userId);
        // Return ideas in newest-first order
        Collections.reverse(ideaIds);
        for (String ideaId : ideaIds) {
            Optional<Idea> idea = findIdea(ideaId);
            if (idea.isPresent()) {
                ideas.add(idea.get());
            }
        }
        return ideas;
    }

    public List<Idea> getPostedIdeasForUser(String userId) {
        List<String> ideaIds = getPostedIdeaIdsForUser(userId);
        return getIdeasInList(ideaIds);
    }

    public List<Project> getJoinedProjectsForUser(String userId) {
        List<String> projectIds = getJoinedProjectIdsForUser(userId);
        return getProjectsInList(projectIds);
    }

    public boolean isIdeaSavedByUser(String userId, String ideaId) {
        return getSavedIdeaIdsForUser(userId).contains(ideaId);
    }

    public boolean isUserAdmin(String userId) {
        return userContainer
            .queryItems(
                "SELECT VALUE c.admin FROM c WHERE c.type = 'User' AND c.userId = '" +
                userId +
                "'",
                new CosmosQueryRequestOptions(),
                boolean.class
            )
            .stream()
            .findFirst()
            .get();
    }

    private List<Idea> getIdeasInList(List<String> ideaIds) {
        return postContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Idea' AND c.ideaId IN ('" +
                String.join("', '", ideaIds) +
                "') ORDER BY c.timePosted DESC",
                new CosmosQueryRequestOptions(),
                Idea.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public void deleteUser(String id) {
        userContainer.deleteItem(
            id,
            new PartitionKey(id),
            new CosmosItemRequestOptions()
        );
    }

    // Ideas

    public void createIdea(Idea idea) {
        postContainer.createItem(idea);
        User user = findUser(idea.getAuthorId()).get();
        user.getPostedIdeaIds().add(idea.getId());
        updateUser(idea.getAuthorId(), user);
    }

    public List<Idea> findAllIdeas() {
        return postContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Idea'",
                new CosmosQueryRequestOptions(),
                Idea.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public Optional<Idea> findIdea(String id) {
        return postContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Idea' AND c.ideaId = '" +
                id +
                "'",
                new CosmosQueryRequestOptions(),
                Idea.class
            )
            .stream()
            .findFirst();
    }

    public void updateIdea(Idea idea) {
        postContainer.replaceItem(
            idea,
            idea.getId(),
            new PartitionKey(idea.getId()),
            new CosmosItemRequestOptions()
        );
    }

    public void deleteIdea(String ideaId, String userId) {
        // Delete idea and all associated comments
        PartitionKey partitionKey = new PartitionKey(ideaId);
        List<String> ids = postContainer
            .queryItems(
                "SELECT VALUE c.id FROM c WHERE c.ideaId = '" + ideaId + "'",
                new CosmosQueryRequestOptions(),
                String.class
            )
            .stream()
            .collect(Collectors.toList());
        for (String postId : ids) {
            postContainer.deleteItem(
                postId,
                partitionKey,
                new CosmosItemRequestOptions()
            );
        }

        // Remove ideaId from author's postedIdeaIds list
        User user = findUser(userId).get();
        user.getPostedIdeaIds().remove(ideaId);
        updateUser(userId, user);
    }

    // Comments

    public void createComment(Comment comment) {
        postContainer.createItem(comment);
    }

    public List<Comment> findAllCommentsOnIdea(String ideaId) {
        return postContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Comment' AND c.ideaId = '" +
                ideaId +
                "' ORDER BY c.timePosted DESC",
                new CosmosQueryRequestOptions(),
                Comment.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public Optional<Comment> findCommentOnIdea(
        String ideaId,
        String commentId
    ) {
        return postContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Comment' AND c.ideaId = '" +
                ideaId +
                "' AND c.id = '" +
                commentId +
                "'",
                new CosmosQueryRequestOptions(),
                Comment.class
            )
            .stream()
            .findFirst();
    }

    public void updateComment(Comment comment) {
        postContainer.replaceItem(
            comment,
            comment.getId(),
            new PartitionKey(comment.getIdeaId()),
            new CosmosItemRequestOptions()
        );
    }

    public void deleteComment(String id, String ideaId) {
        postContainer.deleteItem(
            id,
            new PartitionKey(ideaId),
            new CosmosItemRequestOptions()
        );
    }

    // Messages

    public void sendIndividualMessage(
        String senderId,
        String recipientUsername,
        String content
    ) {
        User sender = findUser(senderId).get();
        User recipient = findUserByUsername(recipientUsername).get();
        ReceivedIndividualMessage receivedMessage = new ReceivedIndividualMessage(
            recipient.getId(),
            sender.getUsername(),
            content
        );
        SentIndividualMessage sentMessage = new SentIndividualMessage(
            senderId,
            recipientUsername,
            content
        );
        // TODO: Handle failure here
        userContainer.createItem(receivedMessage);
        userContainer.createItem(sentMessage);
    }

    // TODO: Handle failure if one or more messages fail to save
    public void sendGroupMessage(
        String senderId,
        String recipientProjectId,
        String content
    ) {
        User sender = findUser(senderId).get();
        Project recipientProject = getProject(recipientProjectId).get();
        for (String recipientId : recipientProject.getTeamMemberIds()) {
            // Skip the user sending the message
            if (recipientId.equals(senderId)) {
                continue;
            }
            ReceivedGroupMessage receivedGroupMessage = new ReceivedGroupMessage(
                recipientId,
                sender.getUsername(),
                content,
                recipientProjectId,
                recipientProject.getName()
            );
            userContainer.createItem(receivedGroupMessage);
        }
        SentGroupMessage sentGroupMessage = new SentGroupMessage(
            senderId,
            recipientProjectId,
            recipientProject.getName(),
            content
        );
        userContainer.createItem(sentGroupMessage);
    }

    public void sendAdminGroupMessage(
        String recipientProjectId,
        String content
    ) {
        Project recipientProject = getProject(recipientProjectId).get();
        for (String recipientId : recipientProject.getTeamMemberIds()) {
            ReceivedGroupMessage receivedGroupMessage = new ReceivedGroupMessage(
                recipientId,
                "projectideas",
                content,
                recipientProjectId,
                recipientProject.getName()
            );
            userContainer.createItem(receivedGroupMessage);
        }
    }

    public Optional<ReceivedMessage> findReceivedMessage(
        String recipientId,
        String messageId
    ) {
        return userContainer
            .queryItems(
                "SELECT * FROM c WHERE (c.type = 'ReceivedIndividualMessage' OR c.type = 'ReceivedGroupMessage') " +
                "AND c.userId = '" +
                recipientId +
                "' AND c.id = '" +
                messageId +
                "'",
                new CosmosQueryRequestOptions(),
                ReceivedMessage.class
            )
            .stream()
            .findFirst();
    }

    public List<ReceivedMessage> findAllReceivedMessages(String recipientId) {
        return userContainer
            .queryItems(
                "SELECT * FROM c WHERE (c.type = 'ReceivedIndividualMessage' OR c.type = 'ReceivedGroupMessage') " +
                "AND c.userId = '" +
                recipientId +
                "' ORDER BY c.timeSent DESC",
                new CosmosQueryRequestOptions(),
                ReceivedMessage.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public List<ReceivedMessage> findAllUnreadReceivedMessages(
        String recipientId
    ) {
        return userContainer
            .queryItems(
                "SELECT * FROM c WHERE (c.type = 'ReceivedIndividualMessage' OR c.type = 'ReceivedGroupMessage') " +
                "AND c.userId = '" +
                recipientId +
                "' AND c.unread = true ORDER BY c.timeSent DESC",
                new CosmosQueryRequestOptions(),
                ReceivedMessage.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public List<SentMessage> findAllSentMessages(String senderId) {
        return userContainer
            .queryItems(
                "SELECT * FROM c WHERE (c.type = 'SentIndividualMessage' OR c.type = 'SentGroupMessage') " +
                "AND c.userId = '" +
                senderId +
                "' ORDER BY c.timeSent DESC",
                new CosmosQueryRequestOptions(),
                SentMessage.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public int getNumberOfUnreadMessages(String recipientId) {
        return userContainer
            .queryItems(
                "SELECT VALUE COUNT(1) FROM c WHERE (c.type = 'ReceivedIndividualMessage' OR c.type = 'ReceivedGroupMessage') " +
                "AND c.unread = true AND c.userId = '" +
                recipientId +
                "'",
                new CosmosQueryRequestOptions(),
                Integer.class
            )
            .stream()
            .findFirst()
            .get();
    }

    public void markReceivedMessageAsRead(
        String messageId,
        String recipientId
    ) {
        markReceivedMessage(messageId, recipientId, false);
    }

    public void markReceivedMessageAsUnread(
        String messageId,
        String recipientId
    ) {
        markReceivedMessage(messageId, recipientId, true);
    }

    public void markAllReceivedMessagesAsRead(String recipientId) {
        userContainer
            .queryItems(
                "SELECT VALUE c.id FROM c WHERE c.type = (c.type = 'ReceivedIndividualMessage' OR c.type = 'ReceivedGroupMessage') " +
                "AND c.unread = true AND c.userId = '" +
                recipientId +
                "'",
                new CosmosQueryRequestOptions(),
                String.class
            )
            .stream()
            .forEach(
                messageId -> markReceivedMessageAsRead(messageId, recipientId)
            );
    }

    private void markReceivedMessage(
        String messageId,
        String recipientId,
        boolean unread
    ) {
        ReceivedMessage message = findReceivedMessage(recipientId, messageId)
            .get();
        message.setUnread(unread);
        updateReceivedMessage(message);
    }

    public void updateReceivedMessage(ReceivedMessage message) {
        userContainer.replaceItem(
            message,
            message.getId(),
            new PartitionKey(message.getUserId()),
            new CosmosItemRequestOptions()
        );
    }

    public void deleteReceivedMessage(String id, String recipientId) {
        userContainer.deleteItem(
            id,
            new PartitionKey(recipientId),
            new CosmosItemRequestOptions()
        );
    }

    public void deleteSentMessage(String id, String senderId) {
        userContainer.deleteItem(
            id,
            new PartitionKey(senderId),
            new CosmosItemRequestOptions()
        );
    }

    // Projects

    public void createProject(Project project, String projectCreatorId) {
        projectContainer.createItem(project);
        User user = findUser(projectCreatorId).get();
        user.getJoinedProjectIds().add(project.getId());
        updateUser(projectCreatorId, user);
    }

    public Optional<Project> getProject(String projectId) {
        return projectContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Project' AND c.projectId = '" +
                projectId +
                "'",
                new CosmosQueryRequestOptions(),
                Project.class
            )
            .stream()
            .findFirst();
    }

    public List<Project> getProjectsBasedOnIdea(String ideaId) {
        return projectContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Project' AND c.lookingForMembers = true AND c.ideaId = '" +
                ideaId +
                "'",
                new CosmosQueryRequestOptions(),
                Project.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    private List<Project> getProjectsInList(List<String> projectIds) {
        return projectContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Project' AND c.projectId IN ('" +
                String.join("', '", projectIds) +
                "')",
                new CosmosQueryRequestOptions(),
                Project.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public void updateProject(Project project) {
        projectContainer.replaceItem(
            project,
            project.getId(),
            new PartitionKey(project.getProjectId()),
            new CosmosItemRequestOptions()
        );
    }

    public void deleteProject(String projectId) {
        projectContainer.deleteItem(
            projectId,
            new PartitionKey(projectId),
            new CosmosItemRequestOptions()
        );
    }
}
