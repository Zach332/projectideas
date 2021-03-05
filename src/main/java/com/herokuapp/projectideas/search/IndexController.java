package com.herokuapp.projectideas.search;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.tag.Tag;
import com.herokuapp.projectideas.database.document.vote.Votable;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FeatureField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.NumericUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class IndexController {

    @Autowired
    private IndexWriter ideaIndexWriter;

    @Autowired
    private IndexWriter projectIndexWriter;

    @Autowired
    private IndexWriter tagIndexWriter;

    @Autowired
    private Database database;

    @PostConstruct
    private void init() {
        List<Idea> ideaList = database.getAllIdeas();
        try {
            indexIdeas(ideaList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Project> projectList = database.getAllPublicProjects();
        try {
            indexProjects(projectList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Tag> tagList = database.getAllTags();
        try {
            indexTags(tagList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void indexIdeas(List<Idea> ideaList) throws IOException {
        List<Document> docs = new ArrayList<Document>();
        for (Idea idea : ideaList) {
            Document doc = new Document();
            doc.add(new TextField("title", idea.getTitle(), Field.Store.YES));
            doc.add(
                new TextField("content", idea.getContent(), Field.Store.YES)
            );
            doc.add(new TextField("id", idea.getId(), Field.Store.YES));
            doc.add(
                new SortedNumericDocValuesField(
                    "recency",
                    NumericUtils.floatToSortableInt(getRecencyScore(idea))
                )
            );
            doc.add(
                new SortedNumericDocValuesField(
                    "upvotes",
                    NumericUtils.floatToSortableInt(getUpvoteScore(idea))
                )
            );
            doc.add(
                new SortedNumericDocValuesField(
                    "hotness",
                    NumericUtils.floatToSortableInt(getHotnessScore(idea))
                )
            );
            docs.add(doc);
        }
        ideaIndexWriter.addDocuments(docs);
        ideaIndexWriter.commit();
    }

    private void indexProjects(List<Project> projectList) throws IOException {
        List<Document> docs = new ArrayList<Document>();
        for (Project project : projectList) {
            Document doc = new Document();
            doc.add(new TextField("name", project.getName(), Field.Store.YES));
            doc.add(
                new TextField(
                    "description",
                    project.getDescription(),
                    Field.Store.YES
                )
            );
            doc.add(new TextField("id", project.getId(), Field.Store.YES));
            doc.add(
                new SortedNumericDocValuesField(
                    "recency",
                    NumericUtils.floatToSortableInt(getRecencyScore(project))
                )
            );
            doc.add(
                new SortedNumericDocValuesField(
                    "upvotes",
                    NumericUtils.floatToSortableInt(getUpvoteScore(project))
                )
            );
            doc.add(
                new SortedNumericDocValuesField(
                    "hotness",
                    NumericUtils.floatToSortableInt(getHotnessScore(project))
                )
            );
            docs.add(doc);
        }
        projectIndexWriter.addDocuments(docs);
        projectIndexWriter.commit();
    }

    private void indexTags(List<Tag> tagList) throws IOException {
        List<Document> docs = new ArrayList<Document>();
        for (Tag tag : tagList) {
            Document doc = new Document();
            doc.add(new TextField("name", tag.getName(), Field.Store.YES));
            doc.add(new FeatureField("features", "usages", tag.getUsages()));
            doc.add(
                new TextField("type", tag.getType().toString(), Field.Store.YES)
            );
            docs.add(doc);
        }
        tagIndexWriter.addDocuments(docs);
        tagIndexWriter.commit();
    }

    public void indexIdea(Idea idea) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", idea.getTitle(), Field.Store.YES));
        doc.add(new TextField("content", idea.getContent(), Field.Store.YES));
        doc.add(new TextField("id", idea.getId(), Field.Store.YES));
        doc.add(
            new SortedNumericDocValuesField(
                "recency",
                NumericUtils.floatToSortableInt(getRecencyScore(idea))
            )
        );
        doc.add(
            new SortedNumericDocValuesField(
                "upvotes",
                NumericUtils.floatToSortableInt(getUpvoteScore(idea))
            )
        );
        doc.add(
            new SortedNumericDocValuesField(
                "hotness",
                NumericUtils.floatToSortableInt(getHotnessScore(idea))
            )
        );
        ideaIndexWriter.addDocument(doc);
        ideaIndexWriter.commit();
    }

    public void indexProject(Project project) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("name", project.getName(), Field.Store.YES));
        doc.add(
            new TextField(
                "description",
                project.getDescription(),
                Field.Store.YES
            )
        );
        doc.add(new TextField("id", project.getId(), Field.Store.YES));
        doc.add(
            new SortedNumericDocValuesField(
                "recency",
                NumericUtils.floatToSortableInt(getRecencyScore(project))
            )
        );
        doc.add(
            new SortedNumericDocValuesField(
                "upvotes",
                NumericUtils.floatToSortableInt(getUpvoteScore(project))
            )
        );
        doc.add(
            new SortedNumericDocValuesField(
                "hotness",
                NumericUtils.floatToSortableInt(getHotnessScore(project))
            )
        );
        projectIndexWriter.addDocument(doc);
        projectIndexWriter.commit();
    }

    private float getUpvoteScore(Votable votable) {
        return (float) Math.log10(votable.getUpvoteCount());
    }

    private float getRecencyScore(Votable votable) {
        return votable.getTimeCreated() - 1134028003;
    }

    private float getHotnessScore(Votable votable) {
        float upvoteScore = getUpvoteScore(votable);
        float recencyScore = getRecencyScore(votable);
        return upvoteScore + (recencyScore / 45000);
    }

    public void deleteIdea(String ideaId) throws IOException {
        ideaIndexWriter.deleteDocuments(SearchController.getIdQuery(ideaId));
        ideaIndexWriter.commit();
    }

    public void deleteProject(String projectId) throws IOException {
        projectIndexWriter.deleteDocuments(
            SearchController.getIdQuery(projectId)
        );
        projectIndexWriter.commit();
    }

    public void tryUpdateIdea(Idea newIdea) {
        tryDeleteIdea(newIdea.getId());
        tryIndexIdea(newIdea);
    }

    public void tryUpdateProject(Project newPoject) {
        tryDeleteProject(newPoject.getId());
        tryIndexProject(newPoject);
    }

    public void tryIndexIdea(Idea idea) {
        try {
            indexIdea(idea);
        } catch (Exception ignored) {}
    }

    public void tryIndexProject(Project project) {
        try {
            indexProject(project);
        } catch (Exception ignored) {}
    }

    public void tryDeleteIdea(String ideaId) {
        try {
            deleteIdea(ideaId);
        } catch (Exception ignored) {}
    }

    public void tryDeleteProject(String projectId) {
        try {
            deleteProject(projectId);
        } catch (Exception ignored) {}
    }
}
