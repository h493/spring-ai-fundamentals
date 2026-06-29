package com.example.learn_spring_ai.service;

import com.example.learn_spring_ai.dto.Joke;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient;

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
