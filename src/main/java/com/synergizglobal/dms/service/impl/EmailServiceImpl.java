package com.synergizglobal.dms.service.impl;

import java.io.IOException;
import java.util.List;

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
public class EmailServiceImpl {

    JavaMailSender javaMailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender) {

        this.javaMailSender = javaMailSender;

    }

//    @Async
    public void sendCorrespondenceEmail(CorrespondenceLetter letter, List<MultipartFile> attachments)
            throws IOException, MessagingException {

        String subject = "New Correspondence Notification - Related to Contract from (Your Organisation)";


        String body = "Category: " + letter.getCategory() + "\n" +
                "Letter Number: " + letter.getLetterName() + "\n" +
                "From:" + "Project Team" + "\n" +
                "Subject: " + letter.getSubject() + "\n" +
                "Due Date:" + letter.getLetterDate() + "\n" +
                "Status: " + letter.getCurrentStatus();


        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Set main recipient
        helper.setTo(letter.getTo());

        // Handle CC if available
        if (letter.getCcRecipient() != null && !letter.getCcRecipient().isEmpty()) {
            helper.setCc(letter.getCcRecipient().toArray(new String[0]));
        }

        helper.setSubject(subject);
        helper.setText(body);

        // Add attachments
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                if (!file.isEmpty()) {
                    // Ensure file content is read into memory before the async execution
                    byte[] fileBytes = file.getBytes();  // Read file bytes once and store them
                    String fileName = file.getOriginalFilename();
                    String contentType = file.getContentType();

                    // Use ByteArrayDataSource to wrap the file bytes and content type
                    ByteArrayDataSource dataSource = new ByteArrayDataSource(fileBytes, contentType);
                    helper.addAttachment(fileName, dataSource);  // Attach file to email
                }
            }
        }

        // Send the email asynchronously
        javaMailSender.send(message);
    }

}
