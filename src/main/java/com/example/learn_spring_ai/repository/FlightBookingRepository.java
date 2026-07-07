package com.example.learn_spring_ai.repository;

import com.example.learn_spring_ai.entity.FlightBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlightBookingRepository extends JpaRepository<FlightBooking, Long> {

    Optional<FlightBooking> findByBookingReference(String bookingReference);
}
