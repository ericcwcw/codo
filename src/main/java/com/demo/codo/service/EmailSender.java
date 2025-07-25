package com.demo.codo.service;

public interface EmailSender {
    void sendVerificationEmail(String toEmail, String verificationLink);
}
