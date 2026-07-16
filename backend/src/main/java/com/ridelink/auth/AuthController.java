package com.ridelink.auth;

import com.ridelink.auth.dto.OtpResponse;
import com.ridelink.auth.dto.RefreshRequest;
import com.ridelink.auth.dto.RequestOtpRequest;
import com.ridelink.auth.dto.TokenResponse;
import com.ridelink.auth.dto.VerifyOtpRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/request-otp")
    public OtpResponse requestOtp(@Valid @RequestBody RequestOtpRequest request) {
        return authService.requestOtp(request.phoneNumber());
    }

    @PostMapping("/verify-otp")
    public TokenResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyOtp(request.phoneNumber(), request.code());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }
}
