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
import com.herokuapp.projectideas.database.document.DocumentPage;
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
import com.herokuapp.projectideas.database.document.user.UserJoinedProject;
import com.herokuapp.projectideas.database.document.user.UserPostedIdea;
import com.herokuapp.projectideas.database.document.user.UserSavedIdea;
import com.herokuapp.projectideas.database.document.user.UsernameIdPair;
import com.herokuapp.projectideas.database.query.GenericQueries;
import com.herokuapp.projectideas.search.IndexController;
import java.util.ArrayList;
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

    private <T> Optional<T> singleDocumentQuery(
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

    private <T> List<T> multipleDocumentQuery(
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

    private <T> DocumentPage<T> pageQuery(
        SelectQuery query,
        CosmosContainer container,
        int pageNum,
        Class<T> classType
    ) {
        if (pageNum < 1) {
            return new DocumentPage<>(new ArrayList<T>(), false);
        }

        // Get ITEMS_PER_PAGE + 1 documents instead of ITEMS_PER_PAGE
        // in order to check if this is the last page.
        // Only return the first ITEMS_PER_PAGE documents.
        List<T> documents = multipleDocumentQuery(
            query.offsetAndLimitResults(
                (pageNum - 1) * ITEMS_PER_PAGE,
                ITEMS_PER_PAGE + 1
            ),
            container,
            classType
        );
        boolean lastPage = documents.size() <= ITEMS_PER_PAGE;
        if (!lastPage) {
            documents.remove(documents.size() - 1);
        }

        return new DocumentPage<>(documents, lastPage);
    }

    private int countQuery(SelectQuery query, CosmosContainer container) {
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

    // TODO: Merge this with multipleDocumentQuery
    private <T> List<T> multipleValueQuery(
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

    /**
     * Returns a page of documents based on a page of partition keys.
     * Will return documents in the order specified in the partition key page.
     * @param <T> Type of document desired
     * @param partitionKeys Page of partition keys
     * @param container Container for query to be executed on
     * @param pageNum Page number
     * @param classType Type of document to request
     * @return Document page of ITEMS_PER_PAGE documents
     */
    private <T extends RootDocument> DocumentPage<T> getDocumentPageFromPartitionKeyPage(
        DocumentPage<String> partitionKeys,
        CosmosContainer container,
        int pageNum,
        Class<T> classType
    ) {
        List<T> documents = multipleDocumentQuery(
            GenericQueries.queryByPartitionKeyList(
                partitionKeys.getDocuments(),
                classType
            ),
            container,
            classType
        );

        // SQL IN queries are not guaranteed in any order, so order the documents retrieved
        // based on the order of the partition keys
        ArrayList<T> orderedDocuments = new ArrayList<>();
        for (String partitionKey : partitionKeys.getDocuments()) {
            Optional<T> document = documents
                .stream()
                .filter(doc -> doc.getPartitionKey().equals(partitionKey))
                .findAny();
            if (document.isPresent()) {
                orderedDocuments.add(document.get());
            } else {
                // TODO: Log failure here, this should not happen without a database error at some point in time
            }
        }

        return new DocumentPage<>(orderedDocuments, partitionKeys.isLastPage());
    }

    // Users

    public void createUser(User user) {
        userContainer.createItem(user);
    }

    public Optional<User> getUser(String userId) {
        return singleDocumentQuery(
            GenericQueries.queryByPartitionKey(userId, User.class),
            userContainer,
            User.class
        );
    }

    public Optional<User> getUserByEmail(String email) {
        return singleDocumentQuery(
            GenericQueries
                .queryByType(User.class)
                .addRestrictions(new RestrictionBuilder().eq("email", email)),
            userContainer,
            User.class
        );
    }

    public Optional<User> getUserByUsername(String username) {
        return singleDocumentQuery(
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
        return getUserByUsername(username).isPresent();
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
            List<PartitionKey> ideaPartitionKeys = multipleValueQuery(
                GenericQueries
                    .queryByType(Idea.class)
                    .valueOf("ideaId")
                    .addRestrictions(
                        new RestrictionBuilder().eq("authorId", user.getId())
                    ),
                postContainer,
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
                    "WHERE ARRAY_CONTAINS(p.teamMembers, { \"userId\": '" +
                    id +
                    "'}, true) OR " +
                    "ARRAY_CONTAINS(p.usersRequestingToJoin, { \"userId\": '" +
                    id +
                    "'}, true)",
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
        UserSavedIdea savedIdea = new UserSavedIdea(userId, ideaId);
        userContainer.createItem(savedIdea);
    }

    public void unsaveIdeaForUser(String ideaId, String userId) {
        UserSavedIdea savedIdea = singleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(userId, UserSavedIdea.class)
                .addRestrictions(new RestrictionBuilder().eq("ideaId", ideaId)),
            userContainer,
            UserSavedIdea.class
        )
            .get();

        userContainer.deleteItem(savedIdea, new CosmosItemRequestOptions());
    }

    public DocumentPage<Idea> getSavedIdeasForUser(String userId, int pageNum) {
        return getDocumentPageFromPartitionKeyPage(
            pageQuery(
                GenericQueries
                    .queryByPartitionKey(userId, UserSavedIdea.class)
                    .valueOf("ideaId")
                    .orderBy("timeSaved", Order.DESC),
                userContainer,
                pageNum,
                String.class
            ),
            postContainer,
            pageNum,
            Idea.class
        );
    }

    public DocumentPage<Idea> getPostedIdeasForUser(
        String userId,
        int pageNum
    ) {
        return getDocumentPageFromPartitionKeyPage(
            pageQuery(
                GenericQueries
                    .queryByPartitionKey(userId, UserPostedIdea.class)
                    .valueOf("ideaId")
                    .orderBy("timePosted", Order.DESC),
                userContainer,
                pageNum,
                String.class
            ),
            postContainer,
            pageNum,
            Idea.class
        );
    }

    public DocumentPage<Project> getJoinedProjectsForUser(
        String userId,
        int pageNum
    ) {
        return getDocumentPageFromPartitionKeyPage(
            pageQuery(
                GenericQueries
                    .queryByPartitionKey(userId, UserJoinedProject.class)
                    .valueOf("projectId")
                    .orderBy("timeJoined", Order.DESC),
                userContainer,
                pageNum,
                String.class
            ),
            projectContainer,
            pageNum,
            Project.class
        );
    }

    public void joinProjectForUser(String userId, String projectId) {
        UserJoinedProject joinedProject = new UserJoinedProject(
            userId,
            projectId
        );
        userContainer.createItem(joinedProject);
    }

    public void leaveProjectForUser(String userId, String projectId) {
        UserJoinedProject joinedProject = singleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(userId, UserJoinedProject.class)
                .addRestrictions(
                    new RestrictionBuilder().eq("projectId", projectId)
                ),
            userContainer,
            UserJoinedProject.class
        )
            .get();
        userContainer.deleteItem(joinedProject, new CosmosItemRequestOptions());
    }

    public boolean isIdeaSavedByUser(String userId, String ideaId) {
        return (
            countQuery(
                GenericQueries
                    .queryByPartitionKey(userId, UserSavedIdea.class)
                    .addRestrictions(
                        new RestrictionBuilder().eq("ideaId", ideaId)
                    ),
                userContainer
            ) >
            0
        );
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
        return multipleDocumentQuery(
            GenericQueries
                .queryByType(Idea.class)
                .addRestrictions(new RestrictionBuilder().eq("deleted", false))
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

        UserPostedIdea postedIdea = new UserPostedIdea(
            idea.getAuthorId(),
            idea.getIdeaId()
        );
        userContainer.createItem(postedIdea);

        indexController.tryIndexIdea(idea);
    }

    public DocumentPage<Idea> getIdeasByPageNum(int pageNum) {
        return pageQuery(
            GenericQueries
                .queryByType(Idea.class)
                .addRestrictions(new RestrictionBuilder().eq("deleted", false))
                .orderBy("timePosted", Order.DESC),
            postContainer,
            pageNum,
            Idea.class
        );
    }

    public DocumentPage<Idea> getIdeasByTagAndPageNum(String tag, int pageNum) {
        return pageQuery(
            GenericQueries
                .queryByType(Idea.class)
                .addRestrictions(new RestrictionBuilder().eq("deleted", false))
                .arrayContains("tags", tag)
                .orderBy("timePosted", Order.DESC),
            postContainer,
            pageNum,
            Idea.class
        );
    }

    public List<Idea> getIdeasInList(List<String> ideaIds) {
        return multipleDocumentQuery(
            GenericQueries
                .queryByType(Idea.class)
                .addRestrictions(
                    new RestrictionBuilder().eq("deleted", false),
                    new RestrictionBuilder().in("ideaId", ideaIds.toArray())
                )
                .orderBy("timePosted", Order.DESC),
            postContainer,
            Idea.class
        );
    }

    public Optional<Idea> getIdea(String id) {
        return singleDocumentQuery(
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

    public void deleteIdea(Idea idea) {
        // Remove idea from index
        indexController.tryDeleteIdea(idea.getIdeaId());

        // Remove ideaId from author's postedIdeaIds list
        UserPostedIdea postedIdea = singleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(idea.getAuthorId(), UserPostedIdea.class)
                .eq("ideaId", idea.getIdeaId()),
            userContainer,
            UserPostedIdea.class
        )
            .get();
        userContainer.deleteItem(postedIdea, new CosmosItemRequestOptions());

        idea.delete();
        postContainer.replaceItem(
            idea,
            idea.getIdeaId(),
            new PartitionKey(idea.getIdeaId()),
            new CosmosItemRequestOptions()
        );
    }

    // Comments

    public void createComment(Comment comment) {
        postContainer.createItem(comment);
    }

    public List<Comment> getAllCommentsOnIdea(String ideaId) {
        return multipleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(ideaId, Comment.class)
                .orderBy("timePosted", Order.DESC),
            postContainer,
            Comment.class
        );
    }

    public Optional<Comment> getCommentOnIdea(String ideaId, String commentId) {
        return singleDocumentQuery(
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
        User recipient = getUserByUsername(recipientUsername).get();
        // TODO: Store count of unread received messages in each user object, use transaction
        // to ensure the count remains accurate
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

    public Optional<ReceivedMessage> getReceivedMessage(
        String recipientId,
        String messageId
    ) {
        return singleDocumentQuery(
            GenericQueries.queryByIdAndPartitionKey(
                messageId,
                recipientId,
                ReceivedMessage.class
            ),
            userContainer,
            ReceivedMessage.class
        );
    }

    public List<ReceivedMessage> getAllReceivedMessages(String recipientId) {
        return multipleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(recipientId, ReceivedMessage.class)
                .orderBy("timeSent", Order.DESC),
            userContainer,
            ReceivedMessage.class
        );
    }

    public List<ReceivedMessage> getAllUnreadReceivedMessages(
        String recipientId
    ) {
        return multipleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(recipientId, ReceivedMessage.class)
                .addRestrictions(new RestrictionBuilder().eq("unread", true))
                .orderBy("timeSent", Order.DESC),
            userContainer,
            ReceivedMessage.class
        );
    }

    public List<SentMessage> getAllSentMessages(String senderId) {
        return multipleDocumentQuery(
            GenericQueries
                .queryByPartitionKey(senderId, SentMessage.class)
                .orderBy("timeSent", Order.DESC),
            userContainer,
            SentMessage.class
        );
    }

    public int getNumberOfUnreadMessages(String recipientId) {
        return countQuery(
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
        multipleValueQuery(
            GenericQueries
                .queryByType(ReceivedMessage.class)
                .valueOf("id")
                .addRestrictions(
                    new RestrictionBuilder().eq("userId", recipientId),
                    new RestrictionBuilder().eq("unread", true)
                ),
            userContainer,
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
        ReceivedMessage message = getReceivedMessage(recipientId, messageId)
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
        return multipleDocumentQuery(
            GenericQueries.queryByType(IdeaTag.class),
            tagContainer,
            IdeaTag.class
        );
    }

    public List<ProjectTag> getProjectTags() {
        return multipleDocumentQuery(
            GenericQueries.queryByType(ProjectTag.class),
            tagContainer,
            ProjectTag.class
        );
    }

    public List<Tag> getAllTags() {
        return multipleDocumentQuery(
            GenericQueries.queryByType(Tag.class),
            tagContainer,
            Tag.class
        );
    }

    public <T extends Tag> Optional<T> getTag(String name, Class<T> classType) {
        return singleDocumentQuery(
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

        UserJoinedProject joinedProject = new UserJoinedProject(
            projectCreatorId,
            project.getProjectId()
        );
        userContainer.createItem(joinedProject);

        if (project.isPublicProject()) indexController.tryIndexProject(project);
    }

    public DocumentPage<Project> getPublicProjectsByPageNum(int pageNum) {
        return pageQuery(
            GenericQueries
                .queryByType(Project.class)
                .addRestrictions(
                    new RestrictionBuilder().eq("publicProject", true)
                )
                .orderBy("timeCreated", Order.DESC),
            projectContainer,
            pageNum,
            Project.class
        );
    }

    public List<Project> getAllPublicProjects() {
        return multipleDocumentQuery(
            GenericQueries
                .queryByType(Project.class)
                .addRestrictions(
                    new RestrictionBuilder().eq("publicProject", true)
                )
                .orderBy("timeCreated", Order.DESC),
            projectContainer,
            Project.class
        );
    }

    public Optional<Project> getProject(String projectId) {
        return singleDocumentQuery(
            GenericQueries.queryByPartitionKey(projectId, Project.class),
            projectContainer,
            Project.class
        );
    }

    public List<Project> getProjectsBasedOnIdea(String ideaId) {
        return multipleDocumentQuery(
            GenericQueries
                .queryByType(Project.class)
                .addRestrictions(new RestrictionBuilder().eq("ideaId", ideaId))
                .orderBy("timeCreated", Order.DESC),
            projectContainer,
            Project.class
        );
    }

    public List<Project> getPublicProjectsLookingForMemberBasedOnIdea(
        String ideaId
    ) {
        return multipleDocumentQuery(
            GenericQueries
                .queryByType(Project.class)
                .addRestrictions(
                    new RestrictionBuilder().eq("ideaId", ideaId),
                    new RestrictionBuilder().eq("publicProject", true),
                    new RestrictionBuilder().eq("lookingForMembers", true)
                )
                .orderBy("timeCreated", Order.DESC),
            projectContainer,
            Project.class
        );
    }

    public DocumentPage<Project> getPublicProjectsByTagAndPageNum(
        String tag,
        int pageNum
    ) {
        return pageQuery(
            GenericQueries
                .queryByType(Project.class)
                .arrayContains("tags", tag)
                .addRestrictions(
                    new RestrictionBuilder().eq("publicProject", true)
                )
                .orderBy("timeCreated", Order.DESC),
            projectContainer,
            pageNum,
            Project.class
        );
    }

    public List<Project> getProjectsInList(List<String> projectIds) {
        return multipleDocumentQuery(
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

    public void updateProject(
        Project project,
        boolean toPublic,
        boolean toPrivate
    ) {
        if (toPublic) indexController.tryIndexProject(project);
        if (toPrivate) indexController.tryDeleteProject(project.getId());
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

        //Remove project from index
        indexController.tryDeleteProject(projectId);
    }
}
