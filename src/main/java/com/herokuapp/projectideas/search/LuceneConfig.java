package com.herokuapp.projectideas.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LuceneConfig {

    private static final String IDEA_LUCENE_INDEX_PATH = "lucene/ideaIndex/";
    private static final String PROJECT_LUCENE_INDEX_PATH =
        "lucene/projectIndex/";
    private static final String TAG_LUCENE_INDEX_PATH = "lucene/tagIndex/";

    @Bean
    public Directory ideaDirectory() throws IOException {
        Path path = Paths.get(IDEA_LUCENE_INDEX_PATH);
        File file = path.toFile();
        if (!file.exists()) {
            // Create the folder if it does not exist
            file.mkdirs();
        }
        return FSDirectory.open(path);
    }

    @Bean
    public Directory projectDirectory() throws IOException {
        Path path = Paths.get(PROJECT_LUCENE_INDEX_PATH);
        File file = path.toFile();
        if (!file.exists()) {
            // Create the folder if it does not exist
            file.mkdirs();
        }
        return FSDirectory.open(path);
    }

    @Bean
    public Directory tagDirectory() throws IOException {
        Path path = Paths.get(TAG_LUCENE_INDEX_PATH);
        File file = path.toFile();
        if (!file.exists()) {
            // Create the folder if it does not exist
            file.mkdirs();
        }
        return FSDirectory.open(path);
    }

    @Bean
    public Analyzer analyzer() throws IOException {
        return CustomAnalyzer
            .builder()
            .withTokenizer("whitespace")
            .addTokenFilter("lowercase")
            .addTokenFilter("stop")
            .addTokenFilter("porterstem")
            .build();
    }

    @Bean
    public IndexWriter ideaIndexWriter(
        Directory ideaDirectory,
        Analyzer analyzer
    ) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(
            ideaDirectory,
            indexWriterConfig
        );
        indexWriter.deleteAll();
        indexWriter.commit();
        return indexWriter;
    }

    @Bean
    public IndexWriter projectIndexWriter(
        Directory projectDirectory,
        Analyzer analyzer
    ) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(
            projectDirectory,
            indexWriterConfig
        );
        indexWriter.deleteAll();
        indexWriter.commit();
        return indexWriter;
    }

    @Bean
    public IndexWriter tagIndexWriter(
        Directory tagDirectory,
        Analyzer analyzer
    ) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(
            tagDirectory,
            indexWriterConfig
        );
        indexWriter.deleteAll();
        indexWriter.commit();
        return indexWriter;
    }

    @Bean
    public SearcherManager ideaSearcherManager(
        Directory ideaDirectory,
        IndexWriter ideaIndexWriter
    ) throws IOException {
        SearcherManager searcherManager = new SearcherManager(
            ideaIndexWriter,
            false,
            false,
            new SearcherFactory()
        );
        return searcherManager;
    }

    @Bean
    public SearcherManager projectSearcherManager(
        Directory projectDirectory,
        IndexWriter projectIndexWriter
    ) throws IOException {
        SearcherManager searcherManager = new SearcherManager(
            projectIndexWriter,
            false,
            false,
            new SearcherFactory()
        );
        return searcherManager;
    }

    @Bean
    public SearcherManager tagSearcherManager(
        Directory tagDirectory,
        IndexWriter tagIndexWriter
    ) throws IOException {
        SearcherManager searcherManager = new SearcherManager(
            tagIndexWriter,
            false,
            false,
            new SearcherFactory()
        );
        return searcherManager;
    }
}
