package com.example.learn_spring_ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RAGServiceTest {

    @Autowired
    private RAGService ragService;

    @Test
    public void testIngestPdfToVectorStore(){
       ragService.ingestPdfToVectorStore();
    }

    @Test
    public void testaskAI(){
        var response = ragService.askAI("Himanshu chhikara experience");
        System.out.println(response);
    }

    @Test
    public  void testAskAIWithAdvisor(){
        String res = ragService.askAIWithAdvisors("What is my name", "Rohit12");
        System.out.println(res);
    }
}
