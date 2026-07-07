package com.example.learn_spring_ai.dto;

import java.time.LocalDate;

public record FlightBookingRequest(
        String passengerName,
        String fromCity,
        String toCity,
        LocalDate departureDate
) {
}
