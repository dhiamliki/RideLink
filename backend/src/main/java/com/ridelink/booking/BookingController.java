package com.ridelink.booking;

import com.ridelink.booking.dto.BookingResponse;
import com.ridelink.booking.dto.CreateBookingRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/offers/{offerId}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse request(@AuthenticationPrincipal UUID userId, @PathVariable UUID offerId,
                                   @Valid @RequestBody(required = false) CreateBookingRequest body) {
        int seats = body == null ? 1 : body.seatsBookedOrDefault();
        return bookingService.request(userId, offerId, seats);
    }

    @GetMapping("/offers/{offerId}/bookings")
    public List<BookingResponse> forOffer(@AuthenticationPrincipal UUID userId, @PathVariable UUID offerId) {
        return bookingService.forOffer(userId, offerId);
    }

    @PostMapping("/bookings/{id}/accept")
    public BookingResponse accept(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return bookingService.accept(userId, id);
    }

    @PostMapping("/bookings/{id}/decline")
    public BookingResponse decline(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return bookingService.decline(userId, id);
    }

    @PostMapping("/bookings/{id}/cancel")
    public BookingResponse cancel(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return bookingService.cancel(userId, id);
    }

    @GetMapping("/bookings/mine")
    public List<BookingResponse> mine(@AuthenticationPrincipal UUID userId) {
        return bookingService.mine(userId);
    }
}
