package com.autoparts.warehouse.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量存储服务
 * 负责知识库文档的加载、分块、嵌入、存储和检索
 *
 * 启动时自动索引 knowledge-base/ 下的所有 .md 文件
 */
@Service
public class VectorStoreService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private ResourceLoader resourceLoader;

    private boolean indexed = false;

    /**
     * 启动时自动索引知识库
     */
    @PostConstruct
    public void init() {
        try {
            indexKnowledgeBase();
        } catch (Exception e) {
            System.err.println("[VectorStore] 知识库索引失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从 classpath 加载知识库文档并建立向量索引
     */
    public void indexKnowledgeBase() {
        try {
            Resource[] resources = ResourcePatternUtils
                    .getResourcePatternResolver(resourceLoader)
                    .getResources("classpath:knowledge-base/*.md");

            if (resources.length == 0) {
                System.out.println("[VectorStore] 知识库为空，跳过索引");
                return;
            }

            // 1. 加载文档
            List<Document> documents = new ArrayList<>();
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                String content = readResource(resource);
                if (content != null && !content.trim().isEmpty()) {
                    Metadata metadata = new Metadata()
                            .put(Document.FILE_NAME, filename)
                            .put("source", "knowledge-base");
                    documents.add(Document.from(content, metadata));
                    System.out.println("[VectorStore] 已加载文档: " + filename + " (" + content.length() + " 字符)");
                }
            }

            if (documents.isEmpty()) {
                System.out.println("[VectorStore] 没有可索引的文档");
                return;
            }

            // 2. 分块：将文档切分为段落
            DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(500, 50);
            List<TextSegment> allSegments = splitter.splitAll(documents);
            System.out.println("[VectorStore] 分块完成，共 " + allSegments.size() + " 个段落");

            // 3. 批量嵌入并存储（DashScope API 限制每批 ≤ 10）
            int batchSize = 8;
            int totalBatches = (allSegments.size() + batchSize - 1) / batchSize;
            for (int i = 0; i < allSegments.size(); i += batchSize) {
                int end = Math.min(i + batchSize, allSegments.size());
                List<TextSegment> batch = allSegments.subList(i, end);

                Response<List<Embedding>> embedResponse = embeddingModel.embedAll(batch);
                List<Embedding> embeddings = embedResponse.content();
                embeddingStore.addAll(embeddings, batch);

                int batchNum = i / batchSize + 1;
                System.out.println("[VectorStore] 批次 " + batchNum + "/" + totalBatches
                        + ": " + batch.size() + " 个段落已索引");
            }

            indexed = true;
            System.out.println("[VectorStore] ✅ 向量索引完成，共 " + documents.size()
                    + " 个文档，" + allSegments.size() + " 个段落已嵌入并存入向量库");

        } catch (Exception e) {
            System.err.println("[VectorStore] 索引过程出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 语义检索：将查询文本嵌入后搜索最相关的 Top-K 文档片段
     *
     * @param query 用户查询文本
     * @param topK  返回的最相关片段数
     * @return 相关文本片段列表（按相似度降序）
     */
    public List<String> search(String query, int topK) {
        if (!indexed) {
            System.out.println("[VectorStore] 索引未就绪，返回空结果");
            return Collections.emptyList();
        }

        try {
            // 1. 将查询转为向量
            Response<Embedding> embedResponse = embeddingModel.embed(query);
            Embedding queryEmbedding = embedResponse.content();

            // 2. 在向量库中搜索
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(topK)
                    .minScore(0.3)
                    .build();

            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

            // 3. 提取匹配的文本片段
            List<String> results = searchResult.matches().stream()
                    .map(EmbeddingMatch::embedded)
                    .map(TextSegment::text)
                    .collect(Collectors.toList());

            System.out.println("[VectorStore] 检索完成: 查询=\"" + query + "\", 命中 " + results.size() + " 条");
            return results;

        } catch (Exception e) {
            System.err.println("[VectorStore] 检索失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 清空并重建索引（用于知识库更新后重新索引）
     */
    public void reIndex() {
        System.out.println("[VectorStore] 清空索引，重新加载...");
        embeddingStore.removeAll();
        indexed = false;
        indexKnowledgeBase();
    }

    /**
     * 是否已索引
     */
    public boolean isIndexed() {
        return indexed;
    }

    /**
     * 读取 Resource 内容为字符串
     */
    private String readResource(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[VectorStore] 读取文件失败: " + resource.getFilename() + " - " + e.getMessage());
            return null;
        }
    }
}
