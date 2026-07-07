package com.example.learn_spring_ai.controller;

import com.example.learn_spring_ai.tool.TravellingTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CharController {

    private final ChatClient chatClient;
    private final TravellingTools travellingTools;

    @PostMapping("/chat")
    public String chat(@RequestBody String message){

        return chatClient.prompt()
                .user(message)
                .tools(travellingTools)
                .call()
                .content();
    }


}


