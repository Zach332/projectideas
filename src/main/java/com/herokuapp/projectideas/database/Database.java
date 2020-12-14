package com.herokuapp.projectideas.database;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.herokuapp.projectideas.database.document.User;
import com.herokuapp.projectideas.database.document.message.ReceivedMessage;
import com.herokuapp.projectideas.database.document.message.SentMessage;
import com.herokuapp.projectideas.database.document.post.Comment;
import com.herokuapp.projectideas.database.document.post.Idea;
import java.util.ArrayList;
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

    public Database(
        @Value("${azure.cosmos.uri}") String uri,
        @Value("${azure.cosmos.key}") String key,
        @Value("${projectideas.collectionPrefix}") String collectionPrefix
    ) {
        client = new CosmosClientBuilder().endpoint(uri).key(key).buildClient();
        database = client.getDatabase("projectideas");
        userContainer = database.getContainer(collectionPrefix + "_users");
        postContainer = database.getContainer(collectionPrefix + "_posts");
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
        user.getSavedIdeaIds().add(ideaId);
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

    public List<Idea> getSavedIdeasForUser(String userId) {
        List<String> ideaIds = userContainer
            .queryItems(
                "SELECT VALUE c.savedIdeaIds FROM c WHERE c.userId = '" +
                userId +
                "'",
                new CosmosQueryRequestOptions(),
                String.class
            )
            .stream()
            .collect(Collectors.toList());

        return getIdeasInList(ideaIds);
    }

    private List<Idea> getIdeasInList(List<String> ideaIds) {
        return postContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Idea' AND c.ideaId IN ('" +
                String.join("', '", ideaIds) +
                "')",
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

    public void deleteIdea(String id) {
        PartitionKey partitionKey = new PartitionKey(id);
        List<String> ids = postContainer
            .queryItems(
                "SELECT VALUE c.id FROM c WHERE c.ideaId = '" + id + "'",
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
                "'",
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

    public void createMessage(
        String senderId,
        String recipientUsername,
        String content
    ) {
        User sender = findUser(senderId).get();
        User recipient = findUserByUsername(recipientUsername).get();
        ReceivedMessage receivedMessage = new ReceivedMessage(
            recipient.getId(),
            sender.getUsername(),
            content
        );
        SentMessage sentMessage = new SentMessage(
            senderId,
            recipientUsername,
            content
        );
        // TODO: Handle failure here
        userContainer.createItem(receivedMessage);
        userContainer.createItem(sentMessage);
    }

    public Optional<ReceivedMessage> findReceivedMessage(
        String recipientId,
        String messageId
    ) {
        return userContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'ReceivedMessage' AND c.userId = '" +
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
                "SELECT * FROM c WHERE c.type = 'ReceivedMessage' AND c.userId = '" +
                recipientId +
                "'",
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
                "SELECT * FROM c WHERE c.type = 'ReceivedMessage' AND c.userId = '" +
                recipientId +
                "' AND c.unread = true",
                new CosmosQueryRequestOptions(),
                ReceivedMessage.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public List<SentMessage> findAllSentMessages(String senderId) {
        return userContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'SentMessage' AND c.userId = '" +
                senderId +
                "'",
                new CosmosQueryRequestOptions(),
                SentMessage.class
            )
            .stream()
            .collect(Collectors.toList());
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
                "SELECT VALUE c.id FROM c WHERE c.type = 'ReceivedMessage' AND c.unread = true AND c.userId = '" +
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
}
