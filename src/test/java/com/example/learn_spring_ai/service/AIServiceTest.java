package com.example.learn_spring_ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class AIServiceTest {

    @Autowired
    private AIService aiService;

    @Test
    public void testGetJoke(){
        var joke = aiService.getJoke("Dogs");
        System.out.println(joke);
    }

    @Test
    public void testEmbedText(){
        var embed = aiService.getEmbedding("This is a big text here");
        System.out.println(embed.length);
        for(float e: embed) System.out.print(e + " ");
    }

    @Test
    public void testVectorStoreRoundTrip(){
        aiService.addDocuments(List.of(
                "Spring AI provides a VectorStore abstraction for RAG.",
                "pgvector stores embeddings inside PostgreSQL.",
                "Dogs are loyal animals that love to play fetch."
        ));

        List<Document> results = aiService.search("How are embeddings stored?", 2);

        System.out.println("Matches: " + results.size());
        results.forEach(d -> System.out.println(d.getScore() + " -> " + d.getText()));
    }
}
