package com.ridelink.ride;

import com.ridelink.ride.dto.PagedResponse;
import com.ridelink.ride.dto.RequestForm;
import com.ridelink.ride.dto.RequestResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponse create(@AuthenticationPrincipal UUID userId, @Valid @RequestBody RequestForm form) {
        return requestService.create(userId, form);
    }

    @PutMapping("/{id}")
    public RequestResponse update(@AuthenticationPrincipal UUID userId, @PathVariable UUID id,
                                  @Valid @RequestBody RequestForm form) {
        return requestService.update(userId, id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        requestService.cancel(userId, id);
    }

    @GetMapping("/{id}")
    public RequestResponse get(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return requestService.get(userId, id);
    }

    @GetMapping
    public PagedResponse<RequestResponse> search(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) String originCity,
            @RequestParam(required = false) String destCity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer minSeats,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return requestService.search(userId, originCity, destCity, date, minSeats, page, Math.min(Math.max(size, 1), 100));
    }
}
