package com.nur.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IngestionService implements CommandLineRunner {

    private final VectorStore vectorStore;
    private final String docsPath;

    public IngestionService(VectorStore vectorStore,
                            @Value("${app.rag.docs-path}") String docsPath) {
        this.vectorStore = vectorStore;
        this.docsPath = docsPath;
    }

    @Override
    public void run(String... args) throws Exception {
        ingestAllPdfs();
    }

    private void ingestAllPdfs() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(docsPath + "*.pdf");

        if (resources.length == 0) {
            log.warn("No PDF files found in: {}", docsPath);
            return;
        }

        TokenTextSplitter splitter = buildSplitter();

        for (Resource resource : resources) {
            String fileName = resource.getFilename();

            // ✅ Reliable check — filter by exact metadata, not semantic search
            if (isFileAlreadyIngested(fileName)) {
                log.info("Skipping already ingested file: {}", fileName);
                continue;
            }

            ingestSinglePdf(resource, fileName, splitter);
        }

        log.info("Ingestion run complete.");
    }

    private boolean isFileAlreadyIngested(String fileName) {
        try {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            List<Document> existing = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query("document")
                            .topK(1)
                            .filterExpression(b.eq("source", fileName).build())
                            .build()
            );
            return !existing.isEmpty();
        } catch (Exception e) {
            // If filter check fails (e.g. collection empty), treat as not ingested
            log.warn("Could not check ingestion status for '{}', will ingest: {}", fileName, e.getMessage());
            return false;
        }
    }

    private void ingestSinglePdf(Resource resource, String fileName, TokenTextSplitter splitter) {
        try {
            log.info("Ingesting: {}", fileName);
            List<Document> pages = new PagePdfDocumentReader(resource).read();
            List<Document> chunks = splitter.apply(pages);

            // ✅ Tag every chunk with source metadata for deduplication and traceability
            chunks.forEach(doc -> {
                doc.getMetadata().put("source", fileName);
                doc.getMetadata().put("type", "pdf");
            });

            vectorStore.add(chunks);
            log.info("Ingested {} chunks from '{}'", chunks.size(), fileName);

        } catch (Exception e) {
            log.error("Failed to ingest '{}': {}", fileName, e.getMessage(), e);
        }
    }

    private TokenTextSplitter buildSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(512)
                .withMinChunkSizeChars(64)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(1000)
                .withKeepSeparator(true)
                .build();
    }
}