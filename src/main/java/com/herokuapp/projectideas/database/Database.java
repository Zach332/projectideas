package com.herokuapp.projectideas.database;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.herokuapp.projectideas.database.document.Comment;
import com.herokuapp.projectideas.database.document.Idea;
import com.herokuapp.projectideas.database.document.Message;
import com.herokuapp.projectideas.database.document.User;
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
    private CosmosContainer messageContainer;

    public Database(
        @Value("${azure.cosmos.uri}") String uri,
        @Value("${azure.cosmos.key}") String key,
        @Value("${projectideas.collectionPrefix}") String collectionPrefix
    ) {
        client = new CosmosClientBuilder().endpoint(uri).key(key).buildClient();
        database = client.getDatabase("projectideas");
        userContainer = database.getContainer(collectionPrefix + "_users");
        postContainer = database.getContainer(collectionPrefix + "_posts");
        messageContainer =
            database.getContainer(collectionPrefix + "_messages");
    }

    // Users

    public void createUser(User user) {
        userContainer.createItem(user);
    }

    public Optional<User> findUser(String id) {
        return userContainer
            .queryItems(
                "SELECT * FROM c WHERE c.id = '" + id + "'",
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
                "SELECT VALUE c.savedIdeaIds FROM c WHERE c.id = '" +
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
                "SELECT * FROM c WHERE c.ideaId IN ('" +
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

    public void createMessage(Message message) {
        messageContainer.createItem(message);
    }

    public Optional<Message> findMessageToUser(
        String recipientId,
        String messageId
    ) {
        return messageContainer
            .queryItems(
                "SELECT * FROM c WHERE c.recipientId = '" +
                recipientId +
                "' AND c.id = '" +
                messageId +
                "'",
                new CosmosQueryRequestOptions(),
                Message.class
            )
            .stream()
            .findFirst();
    }

    public List<Message> findAllMessagesToUser(String recipientId) {
        return messageContainer
            .queryItems(
                "SELECT * FROM c WHERE c.recipientId = '" + recipientId + "'",
                new CosmosQueryRequestOptions(),
                Message.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public List<Message> findAllUnreadMessagesToUser(String recipientId) {
        return messageContainer
            .queryItems(
                "SELECT * FROM c WHERE c.recipientId = '" +
                recipientId +
                "' AND c.unread = true",
                new CosmosQueryRequestOptions(),
                Message.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public void updateMessage(Message message) {
        messageContainer.replaceItem(
            message,
            message.getId(),
            new PartitionKey(message.getRecipientId()),
            new CosmosItemRequestOptions()
        );
    }

    public void deleteMessage(String id, String recipientId) {
        messageContainer.deleteItem(
            id,
            new PartitionKey(recipientId),
            new CosmosItemRequestOptions()
        );
    }
}
