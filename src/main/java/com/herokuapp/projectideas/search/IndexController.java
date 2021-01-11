package com.herokuapp.projectideas.search;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.tag.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class IndexController {

    @Autowired
    private IndexWriter ideaIndexWriter;

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
            docs.add(doc);
        }
        ideaIndexWriter.addDocuments(docs);
        ideaIndexWriter.commit();
    }

    private void indexTags(List<Tag> tagList) throws IOException {
        List<Document> docs = new ArrayList<Document>();
        for (Tag tag : tagList) {
            Document doc = new Document();
            doc.add(new TextField("name", tag.getName(), Field.Store.YES));
            doc.add(new IntPoint("usages", tag.getUsages()));
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
        ideaIndexWriter.addDocument(doc);
        ideaIndexWriter.commit();
    }

    public void deleteIdea(String ideaId) throws IOException {
        ideaIndexWriter.deleteDocuments(SearchController.getIdQuery(ideaId));
        ideaIndexWriter.commit();
    }

    public void tryIndexIdea(Idea idea) {
        try {
            indexIdea(idea);
        } catch (Exception ignored) {}
    }

    public void tryDeleteIdea(String ideaId) {
        try {
            deleteIdea(ideaId);
        } catch (Exception ignored) {}
    }
}
