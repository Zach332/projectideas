package com.herokuapp.projectideas.search;

import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class SearchController {

    @Autowired
    private SearcherManager searcherManager;

    @Autowired
    private Analyzer analyzer;

    public List<Document> searchIndex(String queryString) {
        try {
            searcherManager.maybeRefresh();
            IndexSearcher indexSearcher = searcherManager.acquire();

            Query query = new QueryParser("title", analyzer).parse(queryString);

            TopDocs topDocs = indexSearcher.search(query, 30);
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(indexSearcher.doc(scoreDoc.doc));
            }

            searcherManager.release(indexSearcher);
            return documents;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
