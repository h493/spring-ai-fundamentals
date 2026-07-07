package com.example.learn_spring_ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RAGService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    @Value("classpath:Himanshu_Chhikara_Resume_SDE2.pdf")
    Resource pdfFile;

    public void ingestPdfToVectorStore() {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfFile);
        List<Document> pages = reader.get();

        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                .withChunkSize(200)
                .build();

        List<Document> chunks = tokenTextSplitter.apply(pages);
        vectorStore.add(chunks);
    }

    public String askAI(String prompt){

        // 1. Retrieve the most relevant documents from pgvector for this question.
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(prompt)
                        .topK(4)
                        .filterExpression("file_name == 'Himanshu_Chhikara_Resume_SDE2.pdf'")
                        .build());

        // 2. Stitch their text together as grounding context.
        String context = String.join("\n\n",
                documents.stream().map(Document::getText).toList());

        // 3. Build a RAG prompt that answers strictly from that context.
        String template = """
                Answer the question using only the context below.
                If the answer is not in the context, say you don't know.

                Context:
                {context}

                Question:
                {question}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        String renderedPrompt = promptTemplate.render(Map.of(
                "context", context,
                "question", prompt));

        // 4. Ask the model with the retrieved context injected.
        return chatClient.prompt()
                .user(renderedPrompt)
                .call()
                .content();
    }


    public String askAIWithAdvisors(String prompt, String userId){
        return chatClient.prompt()
                .system(s -> s.text("""
                        You are a friendly, helpful assistant.
                        Greet the user by their name, {name}, and answer clearly and concisely.
                        """)
                        .param("name", userId))
                .user(prompt)
                .advisors(
                      //  new SafeGuardAdvisor(List.of("politics", "gaming")),
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build(),
                        VectorStoreChatMemoryAdvisor.builder(vectorStore)
                                .defaultTopK(4)
                                .build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .filterExpression("file_name == 'Himanshu_Chhikara_Resume_SDE2.pdf'")
                                        .build())
                                .build()
                )
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .call()
                .content();
    }


}
