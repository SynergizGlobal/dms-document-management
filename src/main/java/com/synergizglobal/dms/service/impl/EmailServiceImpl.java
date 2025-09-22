package com.synergizglobal.dms.service.impl;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.synergizglobal.dms.entity.dms.CorrespondenceLetter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

@Service
@Slf4j
public class EmailServiceImpl {

    JavaMailSender javaMailSender;
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final String fromEmail; 
    public EmailServiceImpl(JavaMailSender javaMailSender, 
            @Value("${spring.mail.username}") String fromEmail) {
this.javaMailSender = javaMailSender;
this.fromEmail = fromEmail;
}

    @Async
    public void sendCorrespondenceEmail(CorrespondenceLetter letter, List<MultipartFile> attachments,String baseUrl)
            throws IOException, MessagingException {

        // Subject and body content
        String subject = "New Correspondence Notification - Related to Contract from (Your Organisation)";

        String body = String.format("""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd;">
            <div style="background-color: #004B87; color: white; padding: 15px; font-size: 20px; font-weight: bold;">
                New Correspondence Notification - Related to Contract from (Your Organisation)
            </div>
            <div style="padding: 20px; color: #333;">
                <table style="width: 100%%; margin-top: 15px; font-size: 14px;">
                    <tr><td><strong>Category:</strong></td><td>: %s</td></tr>
                    <tr><td><strong>Letter Number:</strong></td><td>: %s</td></tr>
                    <tr><td><strong>From: Project Team</strong></td><td></td></tr>
                    <tr><td><strong>Subject: </strong></td><td>: %s</td></tr>
                    <tr><td><strong>Due Date: </strong></td><td>: %s</td></tr>
                    <tr><td><strong>Status: </strong></td><td>: %s</td></tr>
                </table>
            </div>
            <div style="background-color: green; padding: 15px; text-align: center;">
                <a href="%s" style="background-color: green; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; margin-right: 10px;">Correspondence Location</a>
            </div>
        </div>
        """,
                letter.getCategory(),
                letter.getLetterNumber(),
                letter.getSubject(),
                (letter.getDueDate() != null ? letter.getDueDate().format(fmt) : "N/A"),
                letter.getCurrentStatus(),
                baseUrl + "/view.html?id=" + letter.getCorrespondenceId()
        );
        // Create the MimeMessage
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);  // 'true' for multipart (attachments)

        // Set main recipient
        helper.setTo(letter.getTo());
        
        helper.setFrom(this.fromEmail);
        // Handle CC if available
        if (letter.getCcRecipient() != null && !letter.getCcRecipient().isEmpty()) {
            String[] ccArray = letter.getCcRecipient().split(",");
            helper.setCc(ccArray);
        }

        // Set subject and body text
        helper.setSubject(subject);
        helper.setText(body, true);

        // Add attachments
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                if (!file.isEmpty()) {
                    // Log file details for debugging
                    String fileName = file.getOriginalFilename();
                    long fileSize = file.getSize();
                    String contentType = file.getContentType();
                    log.info("Attaching file: " + fileName + " | Size: " + fileSize + " | Content-Type: " + contentType);

                    // Handle smaller files with FileSystemResource
                    if (fileSize < 10 * 1024 * 1024) {
                        File tempFile = File.createTempFile("attachment", file.getOriginalFilename());
                        file.transferTo(tempFile);
                        FileSystemResource fileResource = new FileSystemResource(tempFile);
                        helper.addAttachment(fileName, fileResource);
                    } else {
                        // For large files, use ByteArrayDataSource (store in memory)
                        byte[] fileBytes = file.getBytes();
                        ByteArrayDataSource dataSource = new ByteArrayDataSource(fileBytes, contentType);
                        helper.addAttachment(fileName, dataSource);
                    }

                    log.info("Attachment added: " + fileName);
                } else {
                    log.warn("Skipped empty file: " + file.getOriginalFilename());
                }
            }
        } else {
            log.info("No attachments found for this email.");
        }

        // Send the email asynchronously
        try {
            javaMailSender.send(message);
            log.info("Email sent successfully to " + letter.getTo());
        } catch (Exception e) {
            log.error("Error while sending email", e);
            throw e;  // rethrow or handle accordingly
        }
    }
}