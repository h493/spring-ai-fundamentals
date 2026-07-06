package com.example.learn_spring_ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

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


    @Test
    public void testaskAI(){
        var response = aiService.askAI("what is apple?");
        System.out.println(response);
    }

    @Test
    public void testAskAI_answersFromStoredContext(){
        // Seed a fact the model cannot know on its own, so a correct answer
        // proves the answer came from pgvector retrieval (RAG), not prior knowledge.
        aiService.addDocuments(List.of(
                "Project Zephyr is an internal tool that was launched in the year 2026."
        ));

        String answer = aiService.askAI("In which year was Project Zephyr launched?");
        System.out.println("askAI answer: " + answer);

        assertTrue(answer.contains("2026"),
                "Expected the answer to be grounded in the stored document (contain 2026), but got: " + answer);
    }
}
