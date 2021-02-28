package com.herokuapp.projectideas.search;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.DocumentPage;
import com.herokuapp.projectideas.database.document.post.Idea;
import com.herokuapp.projectideas.database.document.project.Project;
import com.herokuapp.projectideas.database.document.tag.IdeaTag;
import com.herokuapp.projectideas.database.document.tag.ProjectTag;
import com.herokuapp.projectideas.database.document.tag.Tag;
import com.herokuapp.projectideas.dto.DTOMapper;
import com.herokuapp.projectideas.dto.post.PreviewIdeaDTO;
import com.herokuapp.projectideas.dto.post.PreviewIdeaPageDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectDTO;
import com.herokuapp.projectideas.dto.project.PreviewProjectPageDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FeatureField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class SearchController {

    @Autowired
    private SearcherManager ideaSearcherManager;

    @Autowired
    private SearcherManager projectSearcherManager;

    @Autowired
    private SearcherManager tagSearcherManager;

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

    private List<Document> searchIdeaIndex(String queryString) {
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

    private List<Document> searchProjectIndex(String queryString) {
        try {
            projectSearcherManager.maybeRefresh();
            IndexSearcher indexSearcher = projectSearcherManager.acquire();

            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
            PhraseQuery.Builder phraseQueryName = new PhraseQuery.Builder();
            PhraseQuery.Builder phraseQueryDescription = new PhraseQuery.Builder();
            phraseQueryName.setSlop(10);
            phraseQueryDescription.setSlop(20);

            String[] terms = queryString.toLowerCase().split("-| ");

            for (String term : terms) {
                phraseQueryName.add(new Term("name", term));
                phraseQueryDescription.add(new Term("description", term));
            }

            booleanQuery.add(phraseQueryName.build(), Occur.SHOULD);
            booleanQuery.add(phraseQueryDescription.build(), Occur.SHOULD);

            TopDocs topDocs = indexSearcher.search(booleanQuery.build(), 30);
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(indexSearcher.doc(scoreDoc.doc));
            }

            projectSearcherManager.release(indexSearcher);
            return documents;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private <T extends Tag> List<Document> searchTagIndex(
        String queryString,
        Class<T> classType
    ) {
        try {
            tagSearcherManager.maybeRefresh();
            IndexSearcher indexSearcher = tagSearcherManager.acquire();

            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

            booleanQuery.add(
                new TermQuery(
                    new Term("type", classType.getSimpleName().toLowerCase())
                ),
                Occur.MUST
            );
            booleanQuery.add(
                new FuzzyQuery(new Term("name", queryString)),
                Occur.MUST
            );
            booleanQuery.add(
                FeatureField.newSaturationQuery("features", "usages"),
                Occur.SHOULD
            );

            TopDocs topDocs = indexSearcher.search(booleanQuery.build(), 5);
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(indexSearcher.doc(scoreDoc.doc));
            }

            tagSearcherManager.release(indexSearcher);
            return documents;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> searchForIdea(String queryString) {
        List<Document> documents = searchIdeaIndex(queryString);
        List<String> ids = documents
            .stream()
            .map(doc -> doc.get("id"))
            .collect(Collectors.toList());
        return ids;
    }

    private List<String> searchForProject(String queryString) {
        List<Document> documents = searchProjectIndex(queryString);
        List<String> ids = documents
            .stream()
            .map(doc -> doc.get("id"))
            .collect(Collectors.toList());
        return ids;
    }

    public PreviewIdeaPageDTO searchForIdeaByPage(
        String queryString,
        String userId,
        int page
    ) {
        List<String> idResults = searchForIdea(queryString);
        boolean isLastPage = page * Database.ITEMS_PER_PAGE >= idResults.size();
        DocumentPage<Idea> ideaResultsPage = database.getPostPageFromIds(
            new DocumentPage<>(idResults, isLastPage),
            page,
            Idea.class
        );

        List<PreviewIdeaDTO> ideaPreviews = ideaResultsPage
            .getDocuments()
            .stream()
            .map(idea -> mapper.previewIdeaDTO(idea, userId, database))
            .collect(Collectors.toList());
        return new PreviewIdeaPageDTO(ideaPreviews, isLastPage);
    }

    public PreviewProjectPageDTO searchForProjectByPage(
        String queryString,
        int page,
        String userId
    ) {
        List<String> idResults = searchForProject(queryString);
        boolean isLastPage = page * Database.ITEMS_PER_PAGE >= idResults.size();
        DocumentPage<Project> projectResultsPage = database.getPostPageFromIds(
            new DocumentPage<>(idResults, isLastPage),
            page,
            Project.class
        );

        List<PreviewProjectDTO> projectPreviews = projectResultsPage
            .getDocuments()
            .stream()
            .map(project -> mapper.previewProjectDTO(project, userId, database))
            .collect(Collectors.toList());
        return new PreviewProjectPageDTO(projectPreviews, isLastPage);
    }

    public List<String> searchForIdeaTags(String queryString) {
        List<Document> documents = searchTagIndex(queryString, IdeaTag.class);
        return documents
            .stream()
            .map(doc -> doc.get("name"))
            .collect(Collectors.toList());
    }

    public List<String> searchForProjectTags(String queryString) {
        List<Document> documents = searchTagIndex(
            queryString,
            ProjectTag.class
        );
        return documents
            .stream()
            .map(doc -> doc.get("name"))
            .collect(Collectors.toList());
    }
}
