package com.ridelink.ride;

import com.ridelink.ride.dto.OfferForm;
import com.ridelink.ride.dto.OfferResponse;
import com.ridelink.ride.dto.PagedResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponse create(@AuthenticationPrincipal UUID userId, @Valid @RequestBody OfferForm form) {
        return offerService.create(userId, form);
    }

    @PutMapping("/{id}")
    public OfferResponse update(@AuthenticationPrincipal UUID userId, @PathVariable UUID id,
                                @Valid @RequestBody OfferForm form) {
        return offerService.update(userId, id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        offerService.cancel(userId, id);
    }

    @GetMapping("/{id}")
    public OfferResponse get(@PathVariable UUID id) {
        return offerService.get(id);
    }

    @GetMapping
    public PagedResponse<OfferResponse> search(
            @RequestParam(required = false) String originCity,
            @RequestParam(required = false) String destCity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer minSeats,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean smokingAllowed,
            @RequestParam(required = false) Boolean petsAllowed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return offerService.search(originCity, destCity, date, minSeats, maxPrice, smokingAllowed,
                petsAllowed, page, Math.min(Math.max(size, 1), 100));
    }
}
