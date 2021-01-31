package com.herokuapp.projectideas.database;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.github.mohitgoyal91.cosmosdbqueryutils.RestrictionBuilder;
import com.github.mohitgoyal91.cosmosdbqueryutils.SelectQuery;
import com.github.mohitgoyal91.cosmosdbqueryutils.utilities.Constants.Order;
import com.herokuapp.projectideas.database.document.RootDocument;
import com.herokuapp.projectideas.database.document.message.ReceivedGroupMessage;
import com.herokuapp.projectideas.database.document.message.ReceivedIndividualMessage;
import com.herokuapp.projectideas.database.document.message.ReceivedMessage;
import com.herokuapp.projectideas.database.document.message.SentGroupMessage;
import com.herokuapp.projectideas.database.document.message.SentIndividualMessage;
import com.herokuapp.projectideas.database.document.message.SentMessage;
import com.herokuapp.projectideas.database.document.post.Comment;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.tag.IdeaTag;
import com.herokuapp.projectideas.database.document.tag.ProjectTag;
import com.herokuapp.projectideas.database.document.tag.Tag;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import com.herokuapp.projectideas.database.query.GenericQueries;
import com.herokuapp.projectideas.search.IndexController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

    private <T extends RootDocument> Optional<T> executeSingleDocumentQuery(
        SelectQuery query,
        CosmosContainer container,
        Class<T> classType
    ) {
        return container
            .queryItems(
                query.createQuery(),
                new CosmosQueryRequestOptions(),
                classType
            )
            .stream()
            .findAny();
    }

    private <T extends RootDocument> List<T> executeMultipleDocumentQuery(
        SelectQuery query,
        CosmosContainer container,
        Class<T> classType
    ) {
        return container
            .queryItems(
                query.createQuery(),
                new CosmosQueryRequestOptions(),
                classType
            )
            .stream()
            .collect(Collectors.toList());
    }

    private <T extends RootDocument> int executeCountQuery(
        SelectQuery query,
        CosmosContainer container
    ) {
        return container
            .queryItems(
                query.count().createQuery(),
                new CosmosQueryRequestOptions(),
                Integer.class
            )
            .stream()
            .findAny()
            .get();
    }

    // Users

    public void createUser(User user) {
        userContainer.createItem(user);
    }

    public Optional<User> getUser(String userId) {
        return executeSingleDocumentQuery(
            GenericQueries.queryByPartitionKey(userId, User.class),
            userContainer,
            User.class
        );
    }

    public Optional<User> findUserByEmail(String email) {
        return executeSingleDocumentQuery(
            GenericQueries
                .queryByType(User.class)
                .addRestrictions(new RestrictionBuilder().eq("email", email)),
            userContainer,
            User.class
        );
    }

    public Optional<User> findUserByUsername(String username) {
        return executeSingleDocumentQuery(
            GenericQueries
                .queryByType(User.class)
                .addRestrictions(
                    new RestrictionBuilder().eq("username", username)
                ),
            userContainer,
            User.class
        );
    }

    public boolean containsUserWithUsername(String username) {
        return findUserByUsername(username).isPresent();
    }

    public void updateUser(String id, User user) {
        User oldUser = getUser(id).get();

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
        User user = getUser(userId).get();
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
        User user = getUser(userId).get();
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
        return getUser(userId).get().isAdmin();
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
        return executeMultipleDocumentQuery(
            GenericQueries
                .queryByType(Idea.class)
                .orderBy("timePosted", Order.DESC),
            postContainer,
            Idea.class
        );
    }

    public void createIdea(Idea idea) {
        for (String tag : idea.getTags()) {
            Optional<IdeaTag> existingTag = getTag(tag, IdeaTag.class);
            if (existingTag.isPresent()) {
                incrementTagUsages(tag, IdeaTag.class);
            } else {
                createTag(new IdeaTag(tag));
            }
        }
        postContainer.createItem(idea);
        User user = getUser(idea.getAuthorId()).get();
        user.getPostedIdeaIds().add(idea.getId());
        updateUser(idea.getAuthorId(), user);
        indexController.tryIndexIdea(idea);
    }

    public int getNumIdeas() {
        return executeCountQuery(
            GenericQueries.queryByType(Idea.class),
            postContainer
        );
    }

    public int getNumIdeasForTag(String tag) {
        return executeCountQuery(
            GenericQueries.queryByType(Idea.class).arrayContains("tags", tag),
            postContainer
        );
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
        return executeMultipleDocumentQuery(
            GenericQueries
                .queryByType(Idea.class)
                .addRestrictions(
                    new RestrictionBuilder().in("ideaId", ideaIds.toArray())
                )
                .orderBy("timePosted", Order.DESC),
            postContainer,
            Idea.class
        );
    }

    public Optional<Idea> findIdea(String id) {
        return executeSingleDocumentQuery(
            GenericQueries.queryByPartitionKey(id, Idea.class),
            postContainer,
            Idea.class
        );
    }

    public void updateIdea(Idea idea) {
        for (String tag : idea.getTags()) {
            Optional<IdeaTag> existingTag = getTag(tag, IdeaTag.class);
            if (existingTag.isPresent()) {
                incrementTagUsages(tag, IdeaTag.class);
            } else {
                createTag(new IdeaTag(tag));
            }
        }
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
        User user = getUser(userId).get();
        user.getPostedIdeaIds().remove(ideaId);
        updateUser(userId, user);
    }

    // Comments

    public void createComment(Comment comment) {
        postContainer.createItem(comment);
    }

    public List<Comment> findAllCommentsOnIdea(String ideaId) {
        return executeMultipleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(ideaId, Comment.class)
                .orderBy("timePosted", Order.DESC),
            postContainer,
            Comment.class
        );
    }

    public Optional<Comment> findCommentOnIdea(
        String ideaId,
        String commentId
    ) {
        return executeSingleDocumentQuery(
            GenericQueries.queryByIdAndPartitionKey(
                commentId,
                ideaId,
                Comment.class
            ),
            postContainer,
            Comment.class
        );
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
        User sender = getUser(senderId).get();
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
        User sender = getUser(senderId).get();
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
        return executeSingleDocumentQuery(
            GenericQueries.queryByIdAndPartitionKey(
                messageId,
                recipientId,
                ReceivedMessage.class
            ),
            userContainer,
            ReceivedMessage.class
        );
    }

    public List<ReceivedMessage> findAllReceivedMessages(String recipientId) {
        return executeMultipleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(recipientId, ReceivedMessage.class)
                .orderBy("timeSent", Order.DESC),
            userContainer,
            ReceivedMessage.class
        );
    }

    public List<ReceivedMessage> findAllUnreadReceivedMessages(
        String recipientId
    ) {
        return executeMultipleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(recipientId, ReceivedMessage.class)
                .addRestrictions(new RestrictionBuilder().eq("unread", true))
                .orderBy("timeSent", Order.DESC),
            userContainer,
            ReceivedMessage.class
        );
    }

    public List<SentMessage> findAllSentMessages(String senderId) {
        return executeMultipleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(senderId, SentMessage.class)
                .orderBy("timeSent", Order.DESC),
            userContainer,
            SentMessage.class
        );
    }

    public int getNumberOfUnreadMessages(String recipientId) {
        return executeCountQuery(
            GenericQueries
                .queryByPartitionKey(recipientId, ReceivedMessage.class)
                .addRestrictions(new RestrictionBuilder().eq("unread", true)),
            userContainer
        );
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

    public List<IdeaTag> getIdeaTags() {
        return executeMultipleDocumentQuery(
            GenericQueries.queryByType(IdeaTag.class),
            tagContainer,
            IdeaTag.class
        );
    }

    public List<ProjectTag> getProjectTags() {
        return executeMultipleDocumentQuery(
            GenericQueries.queryByType(ProjectTag.class),
            tagContainer,
            ProjectTag.class
        );
    }

    public List<Tag> getAllTags() {
        return executeMultipleDocumentQuery(
            GenericQueries.queryByType(Tag.class),
            tagContainer,
            Tag.class
        );
    }

    public <T extends Tag> Optional<T> getTag(String name, Class<T> classType) {
        return executeSingleDocumentQuery(
            GenericQueries.queryByPartitionKey(name, classType),
            tagContainer,
            classType
        );
    }

    public <T extends Tag> void incrementTagUsages(
        String name,
        Class<T> classType
    ) {
        Tag tag = getTag(name, classType).get();
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
            Optional<ProjectTag> existingTag = getTag(tag, ProjectTag.class);
            if (existingTag.isPresent()) {
                incrementTagUsages(tag, ProjectTag.class);
            } else {
                createTag(new ProjectTag(tag));
            }
        }
        projectContainer.createItem(project);
        User user = getUser(projectCreatorId).get();
        user.getJoinedProjectIds().add(project.getId());
        updateUser(projectCreatorId, user);
    }

    public Optional<Project> getProject(String projectId) {
        return executeSingleDocumentQuery(
            GenericQueries.queryByPartitionKey(projectId, Project.class),
            projectContainer,
            Project.class
        );
    }

    public List<Project> getProjectsBasedOnIdea(String ideaId) {
        return executeMultipleDocumentQuery(
            GenericQueries
                .queryByType(Project.class)
                .addRestrictions(new RestrictionBuilder().eq("ideaId", ideaId))
                .orderBy("timeCreated", Order.DESC),
            projectContainer,
            Project.class
        );
    }

    public List<Project> getProjectsLookingForMemberBasedOnIdea(String ideaId) {
        return executeMultipleDocumentQuery(
            GenericQueries
                .queryByType(Project.class)
                .addRestrictions(
                    new RestrictionBuilder().eq("ideaId", ideaId),
                    new RestrictionBuilder().eq("lookingForMembers", true)
                )
                .orderBy("timeCreated", Order.DESC),
            projectContainer,
            Project.class
        );
    }

    public int getNumProjectsForTag(String tag) {
        return executeCountQuery(
            GenericQueries
                .queryByType(Project.class)
                .arrayContains("tags", tag),
            projectContainer
        );
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
        return executeMultipleDocumentQuery(
            GenericQueries
                .queryByType(Project.class)
                .addRestrictions(
                    new RestrictionBuilder()
                    .in("projectId", projectIds.toArray())
                )
                .orderBy("timeCreated", Order.DESC),
            projectContainer,
            Project.class
        );
    }

    public void updateProject(Project project) {
        for (String tag : project.getTags()) {
            Optional<ProjectTag> existingTag = getTag(tag, ProjectTag.class);
            if (existingTag.isPresent()) {
                incrementTagUsages(tag, ProjectTag.class);
            } else {
                createTag(new ProjectTag(tag));
            }
        }
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
