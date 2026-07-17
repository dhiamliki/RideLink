package com.ridelink.safety;

import com.ridelink.safety.dto.BlockedUserResponse;
import com.ridelink.safety.dto.CreateBlockRequest;
import com.ridelink.safety.dto.CreateReportRequest;
import com.ridelink.safety.dto.ReportResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SafetyController {

    private final SafetyService safetyService;

    public SafetyController(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse report(@AuthenticationPrincipal UUID userId,
                                 @Valid @RequestBody CreateReportRequest body) {
        return safetyService.report(userId, body);
    }

    @PostMapping("/blocks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(@AuthenticationPrincipal UUID userId, @Valid @RequestBody CreateBlockRequest body) {
        safetyService.block(userId, body);
    }

    @DeleteMapping("/blocks/{blockedUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(@AuthenticationPrincipal UUID userId, @PathVariable UUID blockedUserId) {
        safetyService.unblock(userId, blockedUserId);
    }

    @GetMapping("/blocks")
    public List<BlockedUserResponse> blocks(@AuthenticationPrincipal UUID userId) {
        return safetyService.listBlocked(userId);
    }
}
