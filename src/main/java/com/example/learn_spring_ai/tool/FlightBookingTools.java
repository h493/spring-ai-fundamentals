package com.example.learn_spring_ai.tool;

import com.example.learn_spring_ai.entity.BookingStatus;
import com.example.learn_spring_ai.entity.FlightBooking;
import com.example.learn_spring_ai.repository.FlightBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class FlightBookingTools {

    private final FlightBookingRepository flightBookingRepository;

    @Tool(description = "Book a flight for a passenger between two cities on a given date. Returns the booking reference and seat.")
    public String bookFlight(
            @ToolParam(description = "Full name of the passenger") String passengerName,
            @ToolParam(description = "Departure city") String fromCity,
            @ToolParam(description = "Destination city") String toCity,
            @ToolParam(description = "Departure date in ISO format yyyy-MM-dd") String departureDate) {

        FlightBooking booking = createBooking(passengerName, fromCity, toCity, LocalDate.parse(departureDate));
        return "Flight booked for %s from %s to %s on %s. Booking reference: %s, seat: %s."
                .formatted(booking.getPassengerName(), booking.getFromCity(), booking.getToCity(),
                        booking.getDepartureDate(), booking.getBookingReference(), booking.getSeatNumber());
    }

    @Tool(description = "Get the details of an existing flight booking using its booking reference.")
    public String getBooking(
            @ToolParam(description = "Booking reference, e.g. FL-ABC123") String bookingReference) {

        return flightBookingRepository.findByBookingReference(bookingReference)
                .map(b -> "Booking %s: %s from %s to %s on %s, seat %s, status %s."
                        .formatted(b.getBookingReference(), b.getPassengerName(), b.getFromCity(), b.getToCity(),
                                b.getDepartureDate(), b.getSeatNumber(), b.getStatus()))
                .orElse("No booking found for reference " + bookingReference + ".");
    }

    @Tool(description = "Cancel an existing flight booking using its booking reference.")
    public String cancelBooking(
            @ToolParam(description = "Booking reference, e.g. FL-ABC123") String bookingReference) {

        return flightBookingRepository.findByBookingReference(bookingReference)
                .map(b -> {
                    b.setStatus(BookingStatus.CANCELLED);
                    flightBookingRepository.save(b);
                    return "Booking " + b.getBookingReference() + " has been cancelled.";
                })
                .orElse("No booking found for reference " + bookingReference + ".");
    }

    public FlightBooking createBooking(String passengerName, String fromCity, String toCity, LocalDate departureDate) {
        FlightBooking booking = FlightBooking.builder()
                .bookingReference(generateReference())
                .passengerName(passengerName)
                .fromCity(fromCity)
                .toCity(toCity)
                .departureDate(departureDate)
                .seatNumber(generateSeat())
                .status(BookingStatus.CONFIRMED)
                .build();
        return flightBookingRepository.save(booking);
    }

    private String generateReference() {
        return "FL-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateSeat() {
        int row = ThreadLocalRandom.current().nextInt(1, 31);
        char seat = (char) ('A' + ThreadLocalRandom.current().nextInt(0, 6));
        return row + String.valueOf(seat);
    }
}
