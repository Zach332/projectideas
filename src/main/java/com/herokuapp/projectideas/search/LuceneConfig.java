package com.herokuapp.projectideas.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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

    private static final String LUCENE_INDEX_PATH = "lucene/indexDir/";

    @Bean
    public Directory directory() throws IOException {
        Path path = Paths.get(LUCENE_INDEX_PATH);
        File file = path.toFile();
        if (!file.exists()) {
            // Create the folder if it does not exist
            file.mkdirs();
        }
        return FSDirectory.open(path);
    }

    @Bean
    public Analyzer analyzer() {
        return new StandardAnalyzer();
    }

    @Bean
    public IndexWriter indexWriter(Directory directory, Analyzer analyzer)
        throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        indexWriter.deleteAll();
        indexWriter.commit();
        return indexWriter;
    }

    @Bean
    public SearcherManager searcherManager(
        Directory directory,
        IndexWriter indexWriter
    ) throws IOException {
        SearcherManager searcherManager = new SearcherManager(
            indexWriter,
            false,
            false,
            new SearcherFactory()
        );
        return searcherManager;
    }
}
