package com.lifeos.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async("taskExecutor")
    public void sendWelcomeEmail(String toEmail, String name) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[EMAIL START] Thread: " + threadName + " - Sending Welcome email to: " + toEmail);
        try {
            Context context = new Context();
            context.setVariable("name", name);

            String htmlContent = templateEngine.process("email-welcome", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to LifeOS!");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("[EMAIL SUCCESS] Thread: " + threadName + " - Welcome email sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("[EMAIL ERROR] Thread: " + threadName + " - Failed to send Welcome email to: " + toEmail + ". Reason: " + e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void sendOverdueSummaryEmail(String toEmail, String name, List<String> taskTitles) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[EMAIL START] Thread: " + threadName + " - Sending Overdue Alert email to: " + toEmail);
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("taskTitles", taskTitles);

            String htmlContent = templateEngine.process("email-overdue", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Overdue Tasks Alert - LifeOS");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("[EMAIL SUCCESS] Thread: " + threadName + " - Overdue Alert email sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("[EMAIL ERROR] Thread: " + threadName + " - Failed to send Overdue Alert email to: " + toEmail + ". Reason: " + e.getMessage());
        }
    }
}
