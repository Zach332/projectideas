package com.herokuapp.projectideas.database;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.implementation.NotFoundException;
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
import com.herokuapp.projectideas.database.document.vote.IdeaUpvote;
import com.herokuapp.projectideas.database.document.vote.ProjectUpvote;
import com.herokuapp.projectideas.database.document.vote.Upvote;
import com.herokuapp.projectideas.database.document.vote.Votable;
import com.herokuapp.projectideas.database.exception.EmptyPointReadException;
import com.herokuapp.projectideas.database.exception.EmptySingleDocumentQueryException;
import com.herokuapp.projectideas.database.exception.OutdatedDocumentWriteException;
import com.herokuapp.projectideas.database.query.GenericQueries;
import com.herokuapp.projectideas.search.IndexController;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(
        Database.class
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

    private <T> boolean documentExists(
        String id,
        String partitionKey,
        CosmosContainer container,
        Class<T> documentType
    ) {
        try {
            return (
                container
                    .readItem(id, new PartitionKey(partitionKey), documentType)
                    .getStatusCode() ==
                200
            );
        } catch (NotFoundException e) {
            return false;
        }
    }

    private <T> T readDocument(
        String id,
        String partitionKey,
        CosmosContainer container,
        Class<T> documentType
    ) throws EmptyPointReadException {
        try {
            return container
                .readItem(id, new PartitionKey(partitionKey), documentType)
                .getItem();
        } catch (NotFoundException e) {
            throw new EmptyPointReadException(
                documentType.getSimpleName(),
                id,
                partitionKey
            );
        }
    }

    private <T> T singleDocumentQuery(
        SelectQuery query,
        CosmosContainer container,
        Class<T> classType
    ) throws EmptySingleDocumentQueryException {
        return container
            .queryItems(
                query.createQuery(),
                new CosmosQueryRequestOptions(),
                classType
            )
            .stream()
            .findAny()
            .orElseThrow(
                () ->
                    new EmptySingleDocumentQueryException(
                        classType.getSimpleName(),
                        query.createQuery()
                    )
            );
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
                logger.debug(
                    "getDocumentPageFromPartitionKeyPage encountered a document " +
                    "reference to a document that does not exist: \n" +
                    classType.getSimpleName() +
                    " with partition key " +
                    partitionKey
                );
            }
        }

        return new DocumentPage<>(orderedDocuments, partitionKeys.isLastPage());
    }

    public DocumentPage<Idea> getIdeaPageFromIds(
        DocumentPage<String> ids,
        int pageNum
    ) {
        return getDocumentPageFromPartitionKeyPage(
            ids,
            postContainer,
            pageNum,
            Idea.class
        );
    }

    public DocumentPage<Project> getProjectPageFromIds(
        DocumentPage<String> ids,
        int pageNum
    ) {
        return getDocumentPageFromPartitionKeyPage(
            ids,
            projectContainer,
            pageNum,
            Project.class
        );
    }

    private <T extends Upvote<S>, S extends Votable> void upvoteDocument(
        Upvote<S> upvote,
        CosmosContainer container,
        Class<S> documentType
    ) {
        // Only upvote if the user exists
        if (!userExists(upvote.getId())) {
            return;
        }

        // Only upvote if the user has not already upvoted the document
        try {
            container.createItem(upvote);
        } catch (ConflictException e) {
            return;
        }

        S document = container
            .readItem(
                upvote.getPartitionKey(),
                new PartitionKey(upvote.getPartitionKey()),
                documentType
            )
            .getItem();

        document.addUpvote();

        if (document instanceof Idea) {
            indexController.tryUpdateIdea((Idea) document);
        } else if (document instanceof Project) {
            indexController.tryUpdateProject((Project) document);
        }

        container.replaceItem(
            document,
            upvote.getPartitionKey(),
            new PartitionKey(upvote.getPartitionKey()),
            new CosmosItemRequestOptions()
        );
    }

    private <T extends Upvote<S>, S extends Votable> void unupvoteDocument(
        String partitionKey,
        String userId,
        CosmosContainer container,
        Class<T> upvoteType,
        Class<S> documentType
    ) {
        T upvote = container
            .readItem(userId, new PartitionKey(partitionKey), upvoteType)
            .getItem();

        container.deleteItem(upvote, new CosmosItemRequestOptions());

        S document = container
            .readItem(
                upvote.getPartitionKey(),
                new PartitionKey(upvote.getPartitionKey()),
                documentType
            )
            .getItem();

        document.removeUpvote();

        if (document instanceof Idea) {
            indexController.tryUpdateIdea((Idea) document);
        } else if (document instanceof Project) {
            indexController.tryUpdateProject((Project) document);
        }

        container.replaceItem(
            document,
            upvote.getPartitionKey(),
            new PartitionKey(upvote.getPartitionKey()),
            new CosmosItemRequestOptions()
        );
    }

    private <T extends Upvote<S>, S extends Votable> boolean userHasUpvotedDocument(
        String partitionKey,
        String userId,
        CosmosContainer container,
        Class<T> upvoteType
    ) {
        return documentExists(userId, partitionKey, postContainer, upvoteType);
    }

    // Users

    public User createUser(User user) {
        userContainer.createItem(user);
        return user;
    }

    public boolean userExists(String userId) {
        return documentExists(userId, userId, userContainer, User.class);
    }

    public User getUser(String userId) throws EmptyPointReadException {
        return readDocument(userId, userId, userContainer, User.class);
    }

    public User getUserByEmail(String email)
        throws EmptySingleDocumentQueryException {
        return singleDocumentQuery(
            GenericQueries
                .queryByType(User.class)
                .addRestrictions(new RestrictionBuilder().eq("email", email)),
            userContainer,
            User.class
        );
    }

    public boolean userWithUsernameExists(String username) {
        return (
            countQuery(
                GenericQueries
                    .queryByType(User.class)
                    .addRestrictions(
                        new RestrictionBuilder().eq("username", username)
                    ),
                userContainer
            ) >
            0
        );
    }

    public User getUserByUsername(String username)
        throws EmptySingleDocumentQueryException {
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

    public void updateUser(String id, User user)
        throws EmptyPointReadException {
        User oldUser = getUser(id);

        // Handle username denormalization
        if (!user.getUsername().equals(oldUser.getUsername())) {
            ArrayList<Object> params = new ArrayList<Object>();
            params.add(user.getId());
            params.add(user.getUsername());

            CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();

            // Handle posts container
            List<PartitionKey> ideaPartitionKeys = multipleDocumentQuery(
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
        try {
            UserSavedIdea savedIdea = singleDocumentQuery(
                GenericQueries
                    .queryByPartitionKey(userId, UserSavedIdea.class)
                    .addRestrictions(
                        new RestrictionBuilder().eq("ideaId", ideaId)
                    ),
                userContainer,
                UserSavedIdea.class
            );

            userContainer.deleteItem(savedIdea, new CosmosItemRequestOptions());
        } catch (EmptySingleDocumentQueryException e) {
            logger.debug(e.toString());
        }
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

    public boolean userHasSavedIdea(String ideaId, String userId) {
        return (
            countQuery(
                GenericQueries.queryByPartitionKey(userId, UserSavedIdea.class),
                userContainer
            ) >
            0
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
                    .orderBy("timeCreated", Order.DESC),
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
        try {
            UserJoinedProject joinedProject = singleDocumentQuery(
                GenericQueries
                    .queryByPartitionKey(userId, UserJoinedProject.class)
                    .addRestrictions(
                        new RestrictionBuilder().eq("projectId", projectId)
                    ),
                userContainer,
                UserJoinedProject.class
            );

            userContainer.deleteItem(
                joinedProject,
                new CosmosItemRequestOptions()
            );
        } catch (EmptySingleDocumentQueryException e) {
            logger.debug(e.toString());
        }
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

    public boolean isUserAdmin(String userId) throws EmptyPointReadException {
        return getUser(userId).isAdmin();
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
                .orderBy("timeCreated", Order.DESC),
            postContainer,
            Idea.class
        );
    }

    public void createIdea(Idea idea) {
        // Create or update idea tags
        for (String tag : idea.getTags()) {
            // TODO: Improve efficiency here
            // We check if each tag exists and then get that tag again to update it
            if (tagExists(tag, IdeaTag.class)) {
                try {
                    incrementTagUsages(tag, IdeaTag.class);
                } catch (EmptyPointReadException e) {}
            } else {
                createTag(new IdeaTag(tag));
            }
        }

        // Save idea to database
        postContainer.createItem(idea);

        // Update idea index
        indexController.tryIndexIdea(idea);

        // Add initial author upvote
        upvoteIdea(idea.getIdeaId(), idea.getAuthorId());

        // Add UserPostedIdea to user container
        UserPostedIdea postedIdea = new UserPostedIdea(
            idea.getAuthorId(),
            idea.getIdeaId()
        );
        userContainer.createItem(postedIdea);
    }

    public void upvoteIdea(String ideaId, String userId) {
        IdeaUpvote upvote = new IdeaUpvote(ideaId, userId);
        upvoteDocument(upvote, postContainer, Idea.class);
    }

    public void unupvoteIdea(String ideaId, String userId) {
        unupvoteDocument(
            ideaId,
            userId,
            postContainer,
            IdeaUpvote.class,
            Idea.class
        );
    }

    public boolean userHasUpvotedIdea(String ideaId, String userId) {
        return userHasUpvotedDocument(
            ideaId,
            userId,
            postContainer,
            IdeaUpvote.class
        );
    }

    public DocumentPage<Idea> getIdeasByPageNum(int pageNum) {
        return pageQuery(
            GenericQueries
                .queryByType(Idea.class)
                .addRestrictions(new RestrictionBuilder().eq("deleted", false))
                .orderBy("timeCreated", Order.DESC),
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
                .orderBy("timeCreated", Order.DESC),
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
                .orderBy("timeCreated", Order.DESC),
            postContainer,
            Idea.class
        );
    }

    public Idea getIdea(String id) throws EmptyPointReadException {
        return readDocument(id, id, postContainer, Idea.class);
    }

    public void updateIdea(Idea idea) {
        idea.setTimeLastEdited(Instant.now().getEpochSecond());

        indexController.tryUpdateIdea(idea);
        for (String tag : idea.getTags()) {
            // TODO: Change behavior here (and for projects)
            // As it stands, updating an idea without changing the tags will
            // increment the usages of each tag
            if (tagExists(tag, IdeaTag.class)) {
                try {
                    incrementTagUsages(tag, IdeaTag.class);
                } catch (EmptyPointReadException e) {}
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
        UserPostedIdea postedIdea;
        try {
            postedIdea =
                singleDocumentQuery(
                    GenericQueries
                        .queryByPartitionKey(
                            idea.getAuthorId(),
                            UserPostedIdea.class
                        )
                        .eq("ideaId", idea.getIdeaId()),
                    userContainer,
                    UserPostedIdea.class
                );

            userContainer.deleteItem(
                postedIdea,
                new CosmosItemRequestOptions()
            );
        } catch (EmptySingleDocumentQueryException e) {
            logger.debug(e.toString());
        }

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
                .orderBy("timeCreated", Order.DESC),
            postContainer,
            Comment.class
        );
    }

    public Comment getCommentOnIdea(String ideaId, String commentId)
        throws EmptySingleDocumentQueryException {
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
    ) throws EmptyPointReadException, EmptySingleDocumentQueryException {
        User sender = getUser(senderId);
        User recipient = getUserByUsername(recipientUsername);

        recipient.setUnreadMessages(recipient.getUnreadMessages() + 1);
        updateUser(recipient.getUserId(), recipient);

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
    ) throws EmptyPointReadException {
        User sender = getUser(senderId);
        // TODO: Prevent messaging private projects
        Project recipientProject = getProject(recipientProjectId);
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
    ) throws EmptyPointReadException {
        Project recipientProject = getProject(recipientProjectId);
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

    public ReceivedMessage getReceivedMessage(
        String recipientId,
        String messageId
    ) throws EmptyPointReadException {
        return readDocument(
            messageId,
            recipientId,
            userContainer,
            ReceivedMessage.class
        );
    }

    public DocumentPage<ReceivedMessage> getReceivedMessagesByPage(
        String recipientId,
        int pageNum
    ) {
        return pageQuery(
            GenericQueries
                .queryByPartitionKey(recipientId, ReceivedMessage.class)
                .orderBy("timeSent", Order.DESC),
            userContainer,
            pageNum,
            ReceivedMessage.class
        );
    }

    public DocumentPage<SentMessage> getSentMessagesByPage(
        String senderId,
        int pageNum
    ) {
        return pageQuery(
            GenericQueries
                .queryByPartitionKey(senderId, SentMessage.class)
                .orderBy("timeSent", Order.DESC),
            userContainer,
            pageNum,
            SentMessage.class
        );
    }

    public int getNumberOfUnreadMessages(String recipientId)
        throws EmptyPointReadException {
        return getUser(recipientId).getUnreadMessages();
    }

    public void markAllReceivedMessagesAsRead(String recipientId)
        throws EmptyPointReadException {
        User recipient = getUser(recipientId);
        recipient.setUnreadMessages(0);
        updateUser(recipientId, recipient);

        multipleDocumentQuery(
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
                messageId -> {
                    ReceivedMessage message;
                    try {
                        message = getReceivedMessage(recipientId, messageId);
                        message.setUnread(false);
                        updateReceivedMessage(message);
                    } catch (EmptyPointReadException e) {}
                }
            );
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

    public <T extends Tag> boolean tagExists(String name, Class<T> classType) {
        String urlEncodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        return documentExists(
            urlEncodedName,
            // Tag container is partitioned by type
            classType.getSimpleName(),
            tagContainer,
            classType
        );
    }

    public <T extends Tag> T getTag(String name, Class<T> classType)
        throws EmptyPointReadException {
        // The tag id is url encoded to deal with special characters (e.g. #).
        String urlEncodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        return readDocument(
            urlEncodedName,
            // Tag container is partitioned by type
            classType.getSimpleName(),
            tagContainer,
            classType
        );
    }

    public <T extends Tag> void incrementTagUsages(
        String name,
        Class<T> classType
    ) throws EmptyPointReadException {
        Tag tag = getTag(name, classType);
        tag.setUsages(tag.getUsages() + 1);
        tagContainer.replaceItem(
            tag,
            tag.getId(),
            new PartitionKey(classType.getSimpleName()),
            new CosmosItemRequestOptions()
        );
    }

    public void deleteTag(Tag tag) {
        tagContainer.deleteItem(tag, new CosmosItemRequestOptions());
    }

    // Projects

    public void createProject(Project project, String projectCreatorId) {
        for (String tag : project.getTags()) {
            if (tagExists(tag, ProjectTag.class)) {
                try {
                    incrementTagUsages(tag, ProjectTag.class);
                } catch (EmptyPointReadException e) {}
            } else {
                createTag(new ProjectTag(tag));
            }
        }
        projectContainer.createItem(project);

        if (project.isPublicProject()) indexController.tryIndexProject(project);

        upvoteProject(project.getId(), projectCreatorId);

        UserJoinedProject joinedProject = new UserJoinedProject(
            projectCreatorId,
            project.getProjectId()
        );
        userContainer.createItem(joinedProject);
    }

    public void upvoteProject(String projectId, String userId) {
        ProjectUpvote upvote = new ProjectUpvote(projectId, userId);
        upvoteDocument(upvote, projectContainer, Project.class);
    }

    public void unupvoteProject(String projectId, String userId) {
        unupvoteDocument(
            projectId,
            userId,
            projectContainer,
            ProjectUpvote.class,
            Project.class
        );
    }

    public boolean userHasUpvotedProject(String projectId, String userId) {
        return userHasUpvotedDocument(
            projectId,
            userId,
            projectContainer,
            ProjectUpvote.class
        );
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

    // TODO: Should this check for authorization if private?
    // In the event that a previously public project goes private
    public Project getProject(String projectId) throws EmptyPointReadException {
        return readDocument(
            projectId,
            projectId,
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

    public List<Project> getPublicProjectsLookingForMembersBasedOnIdea(
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

    /**
     * Updates project without any protection against overwriting new
     * data that the user has not seen. This is acceptable for boolean
     * fields like isPublic, but not for fields like description.
     * @param project
     * @param toPublic
     * @param toPrivate
     */
    public void updateProject(
        Project project,
        boolean toPublic,
        boolean toPrivate
    ) {
        if (toPublic) {
            indexController.tryIndexProject(project);
        } else if (toPrivate) {
            indexController.tryDeleteProject(project.getId());
        } else {
            indexController.tryUpdateProject(project);
        }
        // TODO: Does we have to check every tag each update?
        // TODO: This code is repeated multiple times
        for (String tag : project.getTags()) {
            if (tagExists(tag, ProjectTag.class)) {
                try {
                    incrementTagUsages(tag, ProjectTag.class);
                } catch (EmptyPointReadException e) {}
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

    /**
     * Updates a project and ensures that later update attempts do not
     * overwrite new data that the user has not seen.
     * @param project
     * @param toPublic
     * @param toPrivate
     * @param timeOfProjectReceipt Time the user updating the project
     * initially received it from the backend
     * @throws OutdatedDocumentWriteException
     */
    public void updateProjectWithConcurrencyControl(
        Project project,
        boolean toPublic,
        boolean toPrivate,
        long timeOfProjectReceipt
    ) throws OutdatedDocumentWriteException {
        // Ensure user is editing the latest version of the project
        if (project.getTimeLastEdited() >= timeOfProjectReceipt) {
            throw new OutdatedDocumentWriteException();
        }
        project.setTimeLastEdited(Instant.now().getEpochSecond());

        updateProject(project, toPublic, toPrivate);
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
