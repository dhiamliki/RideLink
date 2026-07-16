package com.ridelink.auth;

import com.ridelink.auth.dto.OtpResponse;
import com.ridelink.auth.dto.TokenResponse;
import com.ridelink.user.User;
import com.ridelink.user.UserRepository;
import java.time.Instant;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final OtpChallengeRepository otpRepo;
    private final RefreshTokenRepository refreshRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpSender otpSender;
    private final AuthProperties props;
    private final boolean devProfileActive;

    public AuthService(OtpChallengeRepository otpRepo, RefreshTokenRepository refreshRepo,
                       UserRepository userRepo, PasswordEncoder passwordEncoder, JwtService jwtService,
                       OtpSender otpSender, AuthProperties props, Environment environment) {
        this.otpRepo = otpRepo;
        this.refreshRepo = refreshRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpSender = otpSender;
        this.props = props;
        this.devProfileActive = environment.acceptsProfiles(Profiles.of("dev"));
    }

    @Transactional
    public OtpResponse requestOtp(String phoneNumber) {
        Instant now = Instant.now();
        otpRepo.findFirstByPhoneNumberOrderByCreatedAtDesc(phoneNumber).ifPresent(last -> {
            if (last.getCreatedAt().isAfter(now.minus(props.getOtp().getResendInterval()))) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Please wait before requesting another code");
            }
        });

        otpRepo.consumeActiveByPhoneNumber(phoneNumber);

        String code = Tokens.randomOtpCode();
        OtpChallenge challenge = new OtpChallenge(phoneNumber, passwordEncoder.encode(code),
                now.plus(props.getOtp().getTtl()));
        otpRepo.save(challenge);
        otpSender.send(phoneNumber, code);

        return new OtpResponse("OTP sent", devProfileActive ? code : null);
    }

    // noRollbackFor: attemptCount increments and the consumed flag must persist even when we
    // reject the request (wrong code / lockout), so the throttle actually accumulates.
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public TokenResponse verifyOtp(String phoneNumber, String code) {
        Instant now = Instant.now();
        OtpChallenge challenge = otpRepo
                .findFirstByPhoneNumberAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(phoneNumber, now)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active OTP; request a new code"));

        challenge.setAttemptCount(challenge.getAttemptCount() + 1);
        if (challenge.getAttemptCount() > props.getOtp().getMaxAttempts()) {
            challenge.setConsumed(true);
            otpRepo.save(challenge);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many attempts; request a new code");
        }
        if (!passwordEncoder.matches(code, challenge.getCodeHash())) {
            otpRepo.save(challenge);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid code");
        }

        challenge.setConsumed(true);
        otpRepo.save(challenge);

        User existing = userRepo.findByPhoneNumber(phoneNumber).orElse(null);
        boolean isNewUser = existing == null;
        User user = existing != null ? existing : new User(phoneNumber);
        user.setPhoneVerified(true);
        userRepo.save(user);

        return issueTokens(user, isNewUser);
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        Instant now = Instant.now();
        RefreshToken token = refreshRepo.findByTokenHash(Tokens.sha256(rawRefreshToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        token.setRevoked(true);
        refreshRepo.save(token);

        User user = userRepo.findById(token.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return issueTokens(user, false);
    }

    private TokenResponse issueTokens(User user, boolean isNewUser) {
        String accessToken = jwtService.issueAccessToken(user.getId(), user.getPhoneNumber());
        String rawRefresh = Tokens.randomToken();
        refreshRepo.save(new RefreshToken(user.getId(), Tokens.sha256(rawRefresh),
                Instant.now().plus(props.getJwt().getRefreshTtl())));
        return new TokenResponse(accessToken, rawRefresh, isNewUser);
    }
}
