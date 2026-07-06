package com.example.learn_spring_ai.service;

import com.example.learn_spring_ai.dto.Joke;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public float[] getEmbedding(String text){
        return embeddingModel.embed(text);
    }

    public void addDocuments(List<String> texts){
        List<Document> documents = texts.stream()
                .map(Document::new)
                .toList();
        vectorStore.add(documents);
        vectorStore.add(getDummyDocuments());
    }

    public List<Document> search(String query, int topK){
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build());
    }

    public String askAI(String prompt){

        // 1. Retrieve the most relevant documents from pgvector for this question.
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(prompt)
                        .topK(2)
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
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .content();
    }

    public static List<Document> getDummyDocuments() {
        return List.of(
                new Document("user1", Map.of(
                        "name", "Alice",
                        "age", 25,
                        "city", "Delhi"
                )),
                new Document("user2", Map.of(
                        "name", "Bob",
                        "age", 30,
                        "city", "Mumbai"
                )),
                new Document("user3", Map.of(
                        "name", "Charlie",
                        "age", 28,
                        "city", "Bangalore"
                ))
        );
    }

        public String getJoke(String topic){

        String systemPrompt = """
                you are a sarcastic joker , but don't make jokes about politics.
                Give a joke on the topic : {topic}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(systemPrompt);
        String renderText = promptTemplate.render(Map.of("topic", topic));

        var response = chatClient.prompt()
                .user(renderText)
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .entity(Joke.class);


//       return chatClient.prompt()
//               .system("You are a sarcastic joker, give response in one line.")
//                .user("Give me a joke on a topic: " + topic)
//                .call()
//                .content();

        return response.text();
    }
}
