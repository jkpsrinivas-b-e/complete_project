package com.example.emailRegistration.service;

import com.example.emailRegistration.util.OtpDetails;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpService {
    private final Map<String, OtpDetails> otpMap = new ConcurrentHashMap<>();
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    public String generateOtp(String email) {
        String otp = generateSecureOtp();
        long expiryTime = System.currentTimeMillis() + 5 * 60 * 1000;
        otpMap.put(email, new OtpDetails(otp, expiryTime));
        return otp;
    }

    private String generateSecureOtp() {
        char[] otp = new char[OTP_LENGTH];
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp[i] = (char) ('0' + secureRandom.nextInt(10));
        }
        return new String(otp);
    }

    public boolean verifyOtp(String email, String inputOtp) {
        OtpDetails details = otpMap.get(email);
        if (details == null || details.isExpired()) {
            otpMap.remove(email);
            return false;
        }
        if (details.getOtp().equals(inputOtp)) {
            otpMap.remove(email);
            return true;
        }
        return false;
    }
}

