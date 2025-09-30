package com.synergizglobal.dms.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.synergizglobal.dms.repository.dms.SendCorrespondenceLetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;
import com.synergizglobal.dms.entity.dms.SendCorrespondenceLetter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

@Service
@Slf4j

public class EmailServiceImpl {
    @Autowired
    private SendCorrespondenceLetterRepository repository;

    JavaMailSender javaMailSender;
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final String fromEmail;

    public EmailServiceImpl(JavaMailSender javaMailSender,
                            @Value("${spring.mail.username}") String fromEmail) {
        this.javaMailSender = javaMailSender;
        this.fromEmail = fromEmail;
    }

    @Async
    public void sendCorrespondenceEmail(CorrespondenceLetter letter,
                                        String baseUrl, String loggedUserName)
            throws IOException, MessagingException, UnsupportedEncodingException {

        // Subject and body content
        String subject = "New Correspondence Notification - Related to Contract";

        String body = String.format("""
                        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd;">
                            <div style="background-color: #004B87; color: white; padding: 15px; font-size: 20px; font-weight: bold;">
                                Correspondence Letter Details
                            </div>
                            <div style="padding: 20px; color: #333;">
                                <table style="width: 100%%; margin-top: 15px; font-size: 14px;">
                                    <tr><td><strong>Category:</strong></td><td>: %s</td></tr>
                                    <tr><td><strong>Letter Number:</strong></td><td>: %s</td></tr>
                                    <tr><td><strong>From:</strong></td><td>: %s</td></tr>
                                    <tr><td><strong>Subject: </strong></td><td>: %s</td></tr>
                                    <tr><td><strong>Due Date: </strong></td><td>: %s</td></tr>
                                    <tr><td><strong>Status: </strong></td><td>: %s</td></tr>
                                </table>
                            </div>
                            <div style="background-color: green; padding: 15px; text-align: center;">
                                <a href="%s" style="background-color: green; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; margin-right: 10px;">Click Here To Open Correspondence Letter</a>
                            </div>
                        </div>
                        """,
                letter.getCategory(),
                letter.getLetterNumber(),
                loggedUserName,
                letter.getSubject(),
                (letter.getDueDate() != null ? letter.getDueDate().format(fmt) : "N/A"),
                letter.getCurrentStatus(),
                baseUrl + "/view.html?id=" + letter.getCorrespondenceId()
        );

        // Create the MimeMessage
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);  // 'true' for multipart (attachments)
        List<SendCorrespondenceLetter> letters = repository.findBySendCorrespondenceLetter(letter.getCorrespondenceId());
        // 🔹 Collect TO emails (isCC = false)
        log.info("object {}", letter.getSendCorLetters());
        List<String> toEmails = letters.stream()
                .filter(s -> !s.isCC())
                .map(SendCorrespondenceLetter::getToUserEmail)
                .filter(Objects::nonNull)
                .toList();
        log.info("toEmails: {}", toEmails);

        if (toEmails.isEmpty()) {
            throw new IllegalArgumentException("No TO recipients found for correspondence " + letter.getLetterNumber());
        }
        helper.setTo(toEmails.toArray(new String[0]));

        // 🔹 Collect CC emails (isCC = true)
        List<String> ccEmails = letters.stream()
                .filter(SendCorrespondenceLetter::isCC)
                .map(SendCorrespondenceLetter::getToUserEmail)
                .filter(Objects::nonNull)
                .toList();

        if (!ccEmails.isEmpty()) {
            helper.setCc(ccEmails.toArray(new String[0]));
        }

        helper.setFrom(this.fromEmail, loggedUserName);

        helper.setSubject(subject);
        helper.setText(body, true);


        // Send the email asynchronously
        try {
            javaMailSender.send(message);
            log.info("Email sent successfully ");
        } catch (Exception e) {
            log.error("Error while sending email", e);
            throw e;
        }
    }
}