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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FeatureField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
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
    private Analyzer analyzer;

    @Autowired
    DTOMapper mapper;

    public static Query getIdQuery(String id) {
        PhraseQuery.Builder phraseQuery = new PhraseQuery.Builder();
        phraseQuery.add(new Term("id", id));
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

            List<String> terms = tokenizeQuery(queryString);

            for (String term : terms) {
                phraseQueryTitle.add(new Term("title", term));
                phraseQueryContent.add(new Term("content", term));
            }

            booleanQuery.add(phraseQueryTitle.build(), Occur.SHOULD);
            booleanQuery.add(phraseQueryContent.build(), Occur.SHOULD);

            TopDocs topDocs = indexSearcher.search(
                booleanQuery.build(),
                Database.ITEMS_PER_PAGE * 10
            );
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

    private List<Document> getIdeaIndexSortedBy(String scoreType) {
        try {
            ideaSearcherManager.maybeRefresh();
            IndexSearcher indexSearcher = ideaSearcherManager.acquire();

            Sort sort = new Sort(
                new SortedNumericSortField(scoreType, SortField.Type.LONG, true)
            );
            TopDocs topDocs = indexSearcher.search(
                new MatchAllDocsQuery(),
                100000,
                sort
            );

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

            List<String> terms = tokenizeQuery(queryString);

            for (String term : terms) {
                phraseQueryName.add(new Term("name", term));
                phraseQueryDescription.add(new Term("description", term));
            }

            booleanQuery.add(phraseQueryName.build(), Occur.SHOULD);
            booleanQuery.add(phraseQueryDescription.build(), Occur.SHOULD);

            TopDocs topDocs = indexSearcher.search(
                booleanQuery.build(),
                Database.ITEMS_PER_PAGE * 10
            );
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

    private List<Document> getProjectIndexSortedBy(String scoreType) {
        try {
            projectSearcherManager.maybeRefresh();
            IndexSearcher indexSearcher = projectSearcherManager.acquire();

            Sort sort = new Sort(
                new SortedNumericSortField(scoreType, SortField.Type.LONG, true)
            );
            TopDocs topDocs = indexSearcher.search(
                new MatchAllDocsQuery(),
                100000,
                sort
            );

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
            List<String> terms = tokenizeQuery(queryString);

            for (String term : terms) {
                booleanQuery.add(
                    new FuzzyQuery(new Term("name", term)),
                    Occur.MUST
                );
            }
            booleanQuery.add(
                new TermQuery(
                    new Term("type", classType.getSimpleName().toLowerCase())
                ),
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

    private List<String> getIdeasSortedBy(String scoreType) {
        List<Document> documents = getIdeaIndexSortedBy(scoreType);
        List<String> ids = documents
            .stream()
            .map(doc -> doc.get("id"))
            .collect(Collectors.toList());
        return ids;
    }

    private List<String> getProjectsSortedBy(String scoreType) {
        List<Document> documents = getProjectIndexSortedBy(scoreType);
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
        return getIdeaPage(idResults, page, userId);
    }

    public PreviewProjectPageDTO searchForProjectByPage(
        String queryString,
        int page,
        String userId
    ) {
        List<String> idResults = searchForProject(queryString);
        return getProjectPage(idResults, page, userId);
    }

    public PreviewIdeaPageDTO getIdeaPageByRecency(int page, String userId) {
        List<String> idResults = getIdeasSortedBy("recency");
        return getIdeaPage(idResults, page, userId);
    }

    public PreviewIdeaPageDTO getIdeaPageByUpvotes(int page, String userId) {
        List<String> idResults = getIdeasSortedBy("upvotes");
        return getIdeaPage(idResults, page, userId);
    }

    public PreviewIdeaPageDTO getIdeaPageByHotness(int page, String userId) {
        List<String> idResults = getIdeasSortedBy("hotness");
        return getIdeaPage(idResults, page, userId);
    }

    public PreviewProjectPageDTO getProjectPageByRecency(
        int page,
        String userId
    ) {
        List<String> idResults = getProjectsSortedBy("recency");
        return getProjectPage(idResults, page, userId);
    }

    public PreviewProjectPageDTO getProjectPageByUpvotes(
        int page,
        String userId
    ) {
        List<String> idResults = getProjectsSortedBy("upvotes");
        return getProjectPage(idResults, page, userId);
    }

    public PreviewProjectPageDTO getProjectPageByHotness(
        int page,
        String userId
    ) {
        List<String> idResults = getProjectsSortedBy("hotness");
        return getProjectPage(idResults, page, userId);
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

    private PreviewIdeaPageDTO getIdeaPage(
        List<String> idResults,
        int page,
        String userId
    ) {
        boolean isLastPage = page * Database.ITEMS_PER_PAGE >= idResults.size();
        DocumentPage<Idea> ideaResultsPage = database.getIdeaPageFromIds(
            new DocumentPage<>(clampIdListToPage(idResults, page), isLastPage),
            page
        );

        List<PreviewIdeaDTO> ideaPreviews = ideaResultsPage
            .getDocuments()
            .stream()
            .map(idea -> mapper.previewIdeaDTO(idea, userId, database))
            .collect(Collectors.toList());
        return new PreviewIdeaPageDTO(ideaPreviews, isLastPage);
    }

    private PreviewProjectPageDTO getProjectPage(
        List<String> idResults,
        int page,
        String userId
    ) {
        boolean isLastPage = page * Database.ITEMS_PER_PAGE >= idResults.size();
        DocumentPage<Project> projectResultsPage = database.getProjectPageFromIds(
            new DocumentPage<>(clampIdListToPage(idResults, page), isLastPage),
            page
        );

        List<PreviewProjectDTO> projectPreviews = projectResultsPage
            .getDocuments()
            .stream()
            .map(project -> mapper.previewProjectDTO(project, userId, database))
            .collect(Collectors.toList());
        return new PreviewProjectPageDTO(projectPreviews, isLastPage);
    }

    private List<String> clampIdListToPage(List<String> ids, int page) {
        return ids.subList(
            clamp((page - 1) * Database.ITEMS_PER_PAGE, ids.size()),
            clamp(page * Database.ITEMS_PER_PAGE, ids.size())
        );
    }

    private List<String> tokenizeQuery(String query) {
        ArrayList<String> tokenizedStrings = new ArrayList<>();
        TokenStream ts = analyzer.tokenStream(
            "myfield",
            new StringReader(query)
        );
        try {
            try {
                ts.reset();
                while (ts.incrementToken()) {
                    tokenizedStrings.add(
                        ts.getAttribute(CharTermAttribute.class).toString()
                    );
                }
                ts.end();
            } finally {
                ts.close();
            }
            return tokenizedStrings;
        } catch (Exception ignored) {
            return tokenizedStrings;
        }
    }

    private int clamp(int value, int maximum) {
        if (value < 0) return 0;
        if (value > maximum) return maximum;
        return value;
    }
}
