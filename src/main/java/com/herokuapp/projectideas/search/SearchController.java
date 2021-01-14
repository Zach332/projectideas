package com.herokuapp.projectideas.search;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.post.PreviewIdeaDTO;
import com.herokuapp.projectideas.dto.post.PreviewIdeaPageDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
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
    private SearcherManager ideaSearcherManager;

    @Autowired
    private Database database;

    @Autowired
    DTOMapper mapper;

    public static Query getIdQuery(String id) {
        PhraseQuery.Builder phraseQuery = new PhraseQuery.Builder();
        String[] terms = id.split("-");
        for (String term : terms) {
            phraseQuery.add(new Term("id", term));
        }
        return phraseQuery.build();
    }

    private List<Document> searchIndex(String queryString) {
        try {
            ideaSearcherManager.maybeRefresh();
            IndexSearcher indexSearcher = ideaSearcherManager.acquire();

            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
            PhraseQuery.Builder phraseQueryTitle = new PhraseQuery.Builder();
            PhraseQuery.Builder phraseQueryContent = new PhraseQuery.Builder();
            phraseQueryTitle.setSlop(10);
            phraseQueryContent.setSlop(20);

            String[] terms = queryString.toLowerCase().split("-| ");

            for (String term : terms) {
                phraseQueryTitle.add(new Term("title", term));
                phraseQueryContent.add(new Term("content", term));
            }

            booleanQuery.add(phraseQueryTitle.build(), Occur.SHOULD);
            booleanQuery.add(phraseQueryContent.build(), Occur.SHOULD);

            TopDocs topDocs = indexSearcher.search(booleanQuery.build(), 30);
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(indexSearcher.doc(scoreDoc.doc));
            }

            ideaSearcherManager.release(indexSearcher);
            return documents;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Idea> searchForIdea(String queryString) {
        List<Document> documents = searchIndex(queryString);
        List<String> ids = documents
            .stream()
            .map(doc -> doc.get("id"))
            .collect(Collectors.toList());
        List<Idea> unorderedIdeas = database.getIdeasInList(ids);

        List<Idea> orderedIdeas = new ArrayList<Idea>();
        for (String id : ids) {
            Optional<Idea> ideaToAdd = unorderedIdeas
                .stream()
                .filter(idea -> idea.getId().equals(id))
                .findFirst();
            if (ideaToAdd.isPresent()) {
                orderedIdeas.add(ideaToAdd.get());
            }
        }
        return orderedIdeas;
    }

    public PreviewIdeaPageDTO searchForIdeaByPage(
        String queryString,
        int page
    ) {
        List<Idea> allResults = searchForIdea(queryString);
        List<Idea> pageResults = allResults.subList(
            clamp((page - 1) * Database.IDEAS_PER_PAGE, allResults.size()),
            clamp(page * Database.IDEAS_PER_PAGE, allResults.size())
        );
        List<PreviewIdeaDTO> ideaPreviews = pageResults
            .stream()
            .map(idea -> mapper.previewIdeaDTO(idea))
            .collect(Collectors.toList());
        return new PreviewIdeaPageDTO(
            ideaPreviews,
            page * Database.IDEAS_PER_PAGE >= allResults.size()
        );
    }

    private int clamp(int value, int maximum) {
        if (value < 0) return 0;
        if (value > maximum) return maximum;
        return value;
    }
}