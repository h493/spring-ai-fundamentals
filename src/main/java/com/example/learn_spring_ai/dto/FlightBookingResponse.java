package com.example.learn_spring_ai.dto;

import com.example.learn_spring_ai.entity.BookingStatus;
import com.example.learn_spring_ai.entity.FlightBooking;

import java.time.LocalDate;

public record FlightBookingResponse(
        String bookingReference,
        String passengerName,
        String fromCity,
        String toCity,
        LocalDate departureDate,
        String seatNumber,
        BookingStatus status
) {
    public static FlightBookingResponse from(FlightBooking booking) {
        return new FlightBookingResponse(
                booking.getBookingReference(),
                booking.getPassengerName(),
                booking.getFromCity(),
                booking.getToCity(),
                booking.getDepartureDate(),
                booking.getSeatNumber(),
                booking.getStatus()
        );
    }
}
