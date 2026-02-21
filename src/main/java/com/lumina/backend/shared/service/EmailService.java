package com.lumina.backend.shared.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    public void sendPrescriptionEmail(String toEmail, String patientName, byte[] pdfBytes, String fileName) {
        try {
            log.info("📧 Email sending started for: {}", toEmail);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("Lumina Health - Your Prescription Details 💊");

            String body = "Dear " + patientName + ",\n\n" +
                    "Please find your e-prescription attached to this email.\n" +
                    "Scan the QR code on the document at your nearest pharmacy.\n\n" +
                    "Wishing you a speedy recovery,\n" +
                    "Lumina Medical Center";

            helper.setText(body);
            helper.addAttachment(fileName, new ByteArrayResource(pdfBytes));

            javaMailSender.send(message);

            log.info("✅ Email sent SUCCESSFULLY to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ Email sending FAILED to: {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }
}