package com.ridelink.auth;

// Delivery abstraction. A real SMS provider implements this in a later phase.
public interface OtpSender {

    void send(String phoneNumber, String code);
}
