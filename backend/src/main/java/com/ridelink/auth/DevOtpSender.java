package com.ridelink.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Stub sender: logs the code instead of sending SMS. Swap for a real provider later.
@Component
public class DevOtpSender implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(DevOtpSender.class);

    @Override
    public void send(String phoneNumber, String code) {
        log.info("OTP for {} is {}", phoneNumber, code);
    }
}
