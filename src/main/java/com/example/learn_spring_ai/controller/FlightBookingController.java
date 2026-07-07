package com.example.learn_spring_ai.controller;

import com.example.learn_spring_ai.dto.FlightBookingRequest;
import com.example.learn_spring_ai.dto.FlightBookingResponse;
import com.example.learn_spring_ai.entity.FlightBooking;
import com.example.learn_spring_ai.repository.FlightBookingRepository;
import com.example.learn_spring_ai.tool.FlightBookingTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightBookingController {

    private final ChatClient chatClient;
    private final FlightBookingTools flightBookingTools;
    private final FlightBookingRepository flightBookingRepository;
    private final ChatMemory chatMemory;

    @PostMapping("/book")
    public FlightBookingResponse book(@RequestBody FlightBookingRequest request) {
        FlightBooking booking = flightBookingTools.createBooking(
                request.passengerName(),
                request.fromCity(),
                request.toCity(),
                request.departureDate());
        return FlightBookingResponse.from(booking);
    }

    @GetMapping("/{bookingReference}")
    public ResponseEntity<FlightBookingResponse> get(@PathVariable String bookingReference) {
        return flightBookingRepository.findByBookingReference(bookingReference)
                .map(FlightBookingResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatClient.prompt()
                .user(message)
                .tools(flightBookingTools)
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build()
                )
                .call()
                .content();
    }
}
