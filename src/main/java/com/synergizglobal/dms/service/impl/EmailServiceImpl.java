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
    public void sendCorrespondenceEmail(CorrespondenceLetter letter, List<MultipartFile> attachments)
            throws IOException, MessagingException {

        // Subject and body content
        String subject = "New Correspondence Notification - Related to Contract from (Your Organisation)";
        String body = "Category: " + letter.getCategory() + "\n" +
                "Letter Number: " + letter.getLetterNumber() + "\n" +
                "From: Project Team" + "\n" +
                "Subject: " + letter.getSubject() + "\n" +
                "Due Date: " + (letter.getDueDate() != null ? letter.getDueDate().format(fmt) : "N/A") + "\n" +
                "Status: " + letter.getCurrentStatus();

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
        helper.setText(body);

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