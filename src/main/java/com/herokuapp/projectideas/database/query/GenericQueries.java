package com.herokuapp.projectideas.database.query;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.herokuapp.projectideas.database.document.RootDocument;
import com.herokuapp.projectideas.database.document.message.Message;
import com.herokuapp.projectideas.database.document.post.Post;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.tag.Tag;
import com.herokuapp.projectideas.database.document.user.User;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;

public class GenericQueries {

    private static final Reflections reflections = new Reflections(
        "com.herokuapp.projectideas.database"
    );

    private static final String USER_CONTAINER_PARTITION_KEY = "userId";
    private static final String POST_CONTAINER_PARTITION_KEY = "ideaId";
    private static final String TAG_CONTAINER_PARTITION_KEY = "name";
    private static final String PROJECT_CONTAINER_PARTITION_KEY = "projectId";

    public static <T extends RootDocument> Optional<T> getDocumentById(
        CosmosContainer container,
        String id,
        Class<T> classType
    ) {
        return getCosmosPagedIterableById(container, id, classType)
            .stream()
            .findAny();
    }

    public static <T extends RootDocument> Optional<T> getDocumentByIdAndPartitionKey(
        CosmosContainer container,
        String id,
        String partitionKey,
        Class<T> classType
    ) {
        return getCosmosPagedIterableByIdAndPartitionKey(
            container,
            id,
            partitionKey,
            classType
        )
            .stream()
            .findAny();
    }

    public static <T extends RootDocument> Optional<T> getDocumentByPartitionKey(
        CosmosContainer container,
        String partitionKey,
        Class<T> classType
    ) {
        return getCosmosPagedIterableByPartitionKey(
            container,
            partitionKey,
            classType
        )
            .stream()
            .findAny();
    }

    public static <T extends RootDocument> List<T> getDocumentsByPartitionKey(
        CosmosContainer container,
        String partitionKey,
        Class<T> classType
    ) {
        return getCosmosPagedIterableByPartitionKey(
            container,
            partitionKey,
            classType
        )
            .stream()
            .collect(Collectors.toList());
    }

    private static <T extends RootDocument> CosmosPagedIterable<T> getCosmosPagedIterableById(
        CosmosContainer container,
        String id,
        Class<T> classType
    ) {
        return container.queryItems(
            "SELECT * FROM c WHERE c.id = '" +
            id +
            "' AND " +
            getTypeFilterClause(classType),
            new CosmosQueryRequestOptions(),
            classType
        );
    }

    private static <T extends RootDocument> CosmosPagedIterable<T> getCosmosPagedIterableByIdAndPartitionKey(
        CosmosContainer container,
        String id,
        String partitionKey,
        Class<T> classType
    ) {
        return container.queryItems(
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
        );
    }

    private static <T extends RootDocument> CosmosPagedIterable<T> getCosmosPagedIterableByPartitionKey(
        CosmosContainer container,
        String partitionKey,
        Class<T> classType
    ) {
        return container.queryItems(
            "SELECT * FROM c WHERE c." +
            getPartitionKey(classType) +
            " = '" +
            partitionKey +
            "' AND " +
            getTypeFilterClause(classType),
            new CosmosQueryRequestOptions(),
            classType
        );
    }

    private static String getPartitionKey(
        Class<? extends RootDocument> classType
    ) {
        if (
            User.class.isAssignableFrom(classType) ||
            Message.class.isAssignableFrom(classType)
        ) {
            return USER_CONTAINER_PARTITION_KEY;
        } else if (Post.class.isAssignableFrom(classType)) {
            return POST_CONTAINER_PARTITION_KEY;
        } else if (Tag.class.isAssignableFrom(classType)) {
            return TAG_CONTAINER_PARTITION_KEY;
        } else if (Project.class.isAssignableFrom(classType)) {
            return PROJECT_CONTAINER_PARTITION_KEY;
        }
        throw new IllegalArgumentException(
            "The class " +
            classType.getName() +
            " does not have an associated partition key."
        );
    }

    private static <T> String getTypeFilterClause(Class<T> classType) {
        Set<Class<? extends T>> classes = reflections.getSubTypesOf(classType);
        classes.add(classType);
        List<String> subClassNames = classes
            .stream()
            .filter(
                classTypeArg ->
                    !Modifier.isAbstract(classTypeArg.getModifiers())
            )
            .map(classTypeArg -> classTypeArg.getSimpleName())
            .collect(Collectors.toList());

        return "c.type IN ('" + String.join("', '", subClassNames) + "')";
    }
}
