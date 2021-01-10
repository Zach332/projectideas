package com.herokuapp.projectideas.search;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.post.Idea;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class IndexController {

    @Autowired
    private IndexWriter indexWriter;

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
    }

    public void indexIdeas(List<Idea> ideaList) throws IOException {
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
        indexWriter.addDocuments(docs);
        indexWriter.commit();
    }

    public void indexIdea(Idea idea) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", idea.getTitle(), Field.Store.YES));
        doc.add(new TextField("content", idea.getContent(), Field.Store.YES));
        doc.add(new TextField("id", idea.getId(), Field.Store.YES));
        indexWriter.addDocument(doc);
        indexWriter.commit();
    }

    public void deleteIdea(String ideaId) throws IOException {
        indexWriter.deleteDocuments(SearchController.getIdQuery(ideaId));
        indexWriter.commit();
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
