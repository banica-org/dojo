package com.dojo.notifications.service.notificationService;

import com.dojo.notifications.service.messageGenerator.mail.MailMessageGenerator;
import com.dojo.notifications.configuration.EmailConfig;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.notification.Notification;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.contest.enums.NotifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private MailMessageGenerator mailMessageGenerator;

    @Override
    public NotifierType getNotificationServiceTypeMapping() {
        return NotifierType.EMAIL;
    }

    // Notify user
    @Override
    public void notify(UserDetails userDetails, Notification notification, Contest contest) {
        String data = notification.getAsEmailNotification(this.mailMessageGenerator);
        sendEmail(userDetails.getEmail(), data, contest);
    }

    // Notify channel
    @Override
    public void notify(Notification notification, Contest contest) {
        String data = notification.getAsEmailNotification(this.mailMessageGenerator);
        contest.getSenseiEmails().forEach(email -> sendEmail(email, data, contest));
    }

    private void sendEmail(String to, String data, Contest contest) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);

            helper.setFrom(emailConfig.getUsername());
            helper.setTo(to);
            helper.setSubject("Leaderboard change for " + contest.getTitle());
            helper.setText(data, true);

            emailSender.send(message);

        } catch (MessagingException e) {
            LOGGER.warn("Email could not be sent: {}", e.getCause().getMessage());
        }
    }
}