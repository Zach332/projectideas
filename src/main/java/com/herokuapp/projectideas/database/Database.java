package com.herokuapp.projectideas.database;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.herokuapp.projectideas.database.document.RootDocument;
import com.herokuapp.projectideas.database.document.message.ReceivedGroupMessage;
import com.herokuapp.projectideas.database.document.message.ReceivedIndividualMessage;
import com.herokuapp.projectideas.database.document.message.ReceivedMessage;
import com.herokuapp.projectideas.database.document.message.SentGroupMessage;
import com.herokuapp.projectideas.database.document.message.SentIndividualMessage;
import com.herokuapp.projectideas.database.document.message.SentMessage;
import com.herokuapp.projectideas.database.document.post.Comment;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.post.Post;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.tag.Tag;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import com.herokuapp.projectideas.search.IndexController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Database {

    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer userContainer;
    private CosmosContainer postContainer;
    private CosmosContainer tagContainer;
    private CosmosContainer projectContainer;

    @Autowired
    IndexController indexController;

    public static final int ITEMS_PER_PAGE = 10;

    private static final String USER_CONTAINER_PARTITION_KEY = "userId";
    private static final String POST_CONTAINER_PARTITION_KEY = "ideaId";
    private static final String TAG_CONTAINER_PARTITION_KEY = "name";
    private static final String PROJECT_CONTAINER_PARTITION_KEY = "projectId";

    private Reflections reflections = new Reflections(
        "com.herokuapp.projectideas.database"
    );

    public Database(
        @Value("${azure.cosmos.uri}") String uri,
        @Value("${azure.cosmos.key}") String key,
        @Value("${projectideas.collectionPrefix}") String collectionPrefix
    ) {
        client = new CosmosClientBuilder().endpoint(uri).key(key).buildClient();
        database = client.getDatabase("projectideas");
        userContainer = database.getContainer(collectionPrefix + "_users");
        postContainer = database.getContainer(collectionPrefix + "_posts");
        tagContainer = database.getContainer(collectionPrefix + "_tags");
        projectContainer =
            database.getContainer(collectionPrefix + "_projects");
    }

    // Generic methods

    private CosmosContainer getContainer(
        Class<? extends RootDocument> classType
    ) {
        if (classType.isAssignableFrom(User.class)) {
            return userContainer;
        } else if (classType.isAssignableFrom(Post.class)) {
            return postContainer;
        } else if (classType.isAssignableFrom(Tag.class)) {
            return tagContainer;
        } else if (classType.isAssignableFrom(Project.class)) {
            return projectContainer;
        }
        throw new IllegalArgumentException(
            "The class " +
            classType.getName() +
            " does not have an associated CosmosContainer."
        );
    }

    private String getPartitionKey(Class<? extends RootDocument> classType) {
        if (classType.isAssignableFrom(User.class)) {
            return USER_CONTAINER_PARTITION_KEY;
        } else if (classType.isAssignableFrom(Post.class)) {
            return POST_CONTAINER_PARTITION_KEY;
        } else if (classType.isAssignableFrom(Tag.class)) {
            return TAG_CONTAINER_PARTITION_KEY;
        } else if (classType.isAssignableFrom(Project.class)) {
            return PROJECT_CONTAINER_PARTITION_KEY;
        }
        throw new IllegalArgumentException(
            "The class " +
            classType.getName() +
            " does not have an associated partition key."
        );
    }

    private <T extends RootDocument> Optional<T> getDocumentById(
        String id,
        Class<T> classType
    ) {
        return getContainer(classType)
            .queryItems(
                "SELECT * FROM c WHERE c.id = '" +
                id +
                "' AND " +
                getTypeFilterClause(classType),
                new CosmosQueryRequestOptions(),
                classType
            )
            .stream()
            .findFirst();
    }

    private <T extends RootDocument> Optional<T> getDocumentByIdAndPartitionKey(
        String id,
        String partitionKey,
        Class<T> classType
    ) {
        return getContainer(classType)
            .queryItems(
                "SELECT * FROM c WHERE c.id = '" +
                id +
                "' AND c." +
                getPartitionKey(classType) +
                " = '" +
                partitionKey +
                "' AND " +
                getTypeFilterClause(classType),
                new CosmosQueryRequestOptions(),
                classType
            )
            .stream()
            .findFirst();
    }

    private <T extends RootDocument> Optional<T> getDocumentByPartitionKey(
        String partitionKey,
        Class<T> classType
    ) {
        return getContainer(classType)
            .queryItems(
                "SELECT * FROM c WHERE c." +
                getPartitionKey(classType) +
                " = '" +
                partitionKey +
                "' AND " +
                getTypeFilterClause(classType),
                new CosmosQueryRequestOptions(),
                classType
            )
            .stream()
            .findFirst();
    }

    private <T> String getTypeFilterClause(Class<T> classType) {
        Set<Class<? extends T>> classes = reflections.getSubTypesOf(classType);
        List<String> subClassNames = classes
            .stream()
            .map(subClassType -> subClassType.getSimpleName())
            .collect(Collectors.toList());

        return "c.type IN (" + String.join(", ", subClassNames) + ")";
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

            // Handle posts container
            List<PartitionKey> ideaPartitionKeys = postContainer
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
            for (PartitionKey partitionKey : ideaPartitionKeys) {
                options.setPartitionKey(partitionKey);
                postContainer
                    .getScripts()
                    .getStoredProcedure("updateUsername")
                    .execute(params, options);
            }

            // Handle projects container
            List<PartitionKey> projectPartitionKeys = projectContainer
                .queryItems(
                    "SELECT VALUE p.projectId FROM p " +
                    "JOIN t IN p.teamMembers " +
                    "JOIN r IN p.usersRequestingToJoin " +
                    "WHERE t.userId = '" +
                    user.getId() +
                    "' OR r.userId = '" +
                    user.getId() +
                    "'",
                    new CosmosQueryRequestOptions(),
                    String.class
                )
                .stream()
                .distinct()
                .map(projectId -> new PartitionKey(projectId))
                .collect(Collectors.toList());
            for (PartitionKey partitionKey : projectPartitionKeys) {
                options.setPartitionKey(partitionKey);
                projectContainer
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

    public void deleteUser(String id) {
        userContainer.deleteItem(
            id,
            new PartitionKey(id),
            new CosmosItemRequestOptions()
        );
    }

    // Ideas

    public List<Idea> getAllIdeas() {
        return postContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Idea' ORDER BY c.timePosted DESC",
                new CosmosQueryRequestOptions(),
                Idea.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public void createIdea(Idea idea) {
        for (String tag : idea.getTags()) {
            Optional<Tag> existingTag = getTag(tag, Tag.Type.Idea);
            if (existingTag.isPresent()) {
                incrementTagUsages(tag, Tag.Type.Idea);
            } else {
                createTag(new Tag(tag, Tag.Type.Idea));
            }
        }
        postContainer.createItem(idea);
        User user = findUser(idea.getAuthorId()).get();
        user.getPostedIdeaIds().add(idea.getId());
        updateUser(idea.getAuthorId(), user);
        indexController.tryIndexIdea(idea);
    }

    public int getNumIdeas() {
        return postContainer
            .queryItems(
                "SELECT VALUE COUNT(1) FROM c WHERE c.type = 'Idea'",
                new CosmosQueryRequestOptions(),
                Integer.class
            )
            .stream()
            .findFirst()
            .get();
    }

    public int getNumIdeasForTag(String tag) {
        return postContainer
            .queryItems(
                "SELECT VALUE COUNT(1) FROM c WHERE c.type = 'Idea' AND ARRAY_CONTAINS(c.tags, '" +
                tag +
                "')",
                new CosmosQueryRequestOptions(),
                Integer.class
            )
            .stream()
            .findFirst()
            .get();
    }

    public int getLastIdeaPageNum() {
        int numIdeas = getNumIdeas();
        return ((numIdeas - 1) / ITEMS_PER_PAGE) + 1;
    }

    public int getLastPageNumForIdeaTag(String tag) {
        int numIdeas = getNumIdeasForTag(tag);
        return ((numIdeas - 1) / ITEMS_PER_PAGE) + 1;
    }

    public List<Idea> findIdeasByPageNum(int pageNum) {
        return postContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Idea' ORDER BY c.timePosted DESC OFFSET " +
                ((pageNum - 1) * ITEMS_PER_PAGE) +
                " LIMIT " +
                ITEMS_PER_PAGE,
                new CosmosQueryRequestOptions(),
                Idea.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public List<Idea> findIdeasByTagAndPageNum(String tag, int pageNum) {
        return postContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Idea' AND ARRAY_CONTAINS(c.tags, '" +
                tag +
                "') ORDER BY c.timePosted DESC OFFSET " +
                ((pageNum - 1) * ITEMS_PER_PAGE) +
                " LIMIT " +
                ITEMS_PER_PAGE,
                new CosmosQueryRequestOptions(),
                Idea.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public List<Idea> getIdeasInList(List<String> ideaIds) {
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

        //Remove idea from index
        indexController.tryDeleteIdea(ideaId);

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

    public void sendIndividualAdminMessage(String recipientId, String content) {
        ReceivedIndividualMessage receivedMessage = new ReceivedIndividualMessage(
            recipientId,
            "projectideas",
            content
        );
        userContainer.createItem(receivedMessage);
    }

    // TODO: Handle failure if one or more messages fail to save
    public void sendGroupMessage(
        String senderId,
        String recipientProjectId,
        String content
    ) {
        User sender = findUser(senderId).get();
        Project recipientProject = getProject(recipientProjectId).get();
        for (UsernameIdPair recipient : recipientProject.getTeamMembers()) {
            String recipientId = recipient.getUserId();
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

    public void sendGroupAdminMessage(
        String recipientProjectId,
        String content
    ) {
        Project recipientProject = getProject(recipientProjectId).get();
        for (UsernameIdPair recipient : recipientProject.getTeamMembers()) {
            String recipientId = recipient.getUserId();
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
                "SELECT VALUE c.id FROM c WHERE (c.type = 'ReceivedIndividualMessage' OR c.type = 'ReceivedGroupMessage') " +
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

    // Tags

    public void createTag(Tag tag) {
        tagContainer.createItem(tag);
    }

    public List<Tag> getIdeaTags() {
        return tagContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Idea'",
                new CosmosQueryRequestOptions(),
                Tag.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public List<Tag> getProjectTags() {
        return tagContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Project'",
                new CosmosQueryRequestOptions(),
                Tag.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public List<Tag> getAllTags() {
        return tagContainer
            .queryItems(
                "SELECT * FROM c",
                new CosmosQueryRequestOptions(),
                Tag.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public Optional<Tag> getTag(String name, Tag.Type type) {
        return tagContainer
            .queryItems(
                "SELECT * FROM c WHERE c.name = '" +
                name +
                "' AND c.type = '" +
                type.toString() +
                "'",
                new CosmosQueryRequestOptions(),
                Tag.class
            )
            .stream()
            .findFirst();
    }

    public void incrementTagUsages(String name, Tag.Type type) {
        Tag tag = tagContainer
            .queryItems(
                "SELECT * FROM c WHERE c.name = '" +
                name +
                "' AND c.type = '" +
                type.toString() +
                "'",
                new CosmosQueryRequestOptions(),
                Tag.class
            )
            .stream()
            .findFirst()
            .get();
        tag.setUsages(tag.getUsages() + 1);
        tagContainer.replaceItem(
            tag,
            tag.getId(),
            new PartitionKey(tag.getName()),
            new CosmosItemRequestOptions()
        );
    }

    public void deleteTag(Tag tag) {
        tagContainer.deleteItem(tag, new CosmosItemRequestOptions());
    }

    // Projects

    public void createProject(Project project, String projectCreatorId) {
        for (String tag : project.getTags()) {
            Optional<Tag> existingTag = getTag(tag, Tag.Type.Project);
            if (existingTag.isPresent()) {
                incrementTagUsages(tag, Tag.Type.Project);
            } else {
                createTag(new Tag(tag, Tag.Type.Project));
            }
        }
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
                "SELECT * FROM c WHERE c.type = 'Project' AND c.ideaId = '" +
                ideaId +
                "' ORDER BY c.timeCreated DESC",
                new CosmosQueryRequestOptions(),
                Project.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public List<Project> getProjectsLookingForMemberBasedOnIdea(String ideaId) {
        return projectContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Project' AND c.lookingForMembers = true AND c.ideaId = '" +
                ideaId +
                "' ORDER BY c.timeCreated DESC",
                new CosmosQueryRequestOptions(),
                Project.class
            )
            .stream()
            .collect(Collectors.toList());
    }

    public int getNumProjectsForTag(String tag) {
        return projectContainer
            .queryItems(
                "SELECT VALUE COUNT(1) FROM c WHERE c.type = 'Project' AND ARRAY_CONTAINS(c.tags, '" +
                tag +
                "')",
                new CosmosQueryRequestOptions(),
                Integer.class
            )
            .stream()
            .findFirst()
            .get();
    }

    public int getLastPageNumForProjectTag(String tag) {
        int numProjects = getNumProjectsForTag(tag);
        return ((numProjects - 1) / ITEMS_PER_PAGE) + 1;
    }

    public List<Project> findProjectsByTagAndPageNum(String tag, int pageNum) {
        return projectContainer
            .queryItems(
                "SELECT * FROM c WHERE c.type = 'Project' AND ARRAY_CONTAINS(c.tags, '" +
                tag +
                "') ORDER BY c.timeCreated DESC OFFSET " +
                ((pageNum - 1) * ITEMS_PER_PAGE) +
                " LIMIT " +
                ITEMS_PER_PAGE,
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
                "') ORDER BY c.timeCreated DESC",
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
