package com.dojo.notifications.service.notificationService;

import com.dojo.notifications.configuration.EmailConfig;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.notification.Notification;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.messageGenerator.mail.MailMessageGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EmailNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailConfig emailConfig;
    private final JavaMailSender emailSender;
    private final Map<NotificationType, MailMessageGenerator> mailMessageGenerators;

    public EmailNotificationService(EmailConfig emailConfig, JavaMailSender emailSender, Collection<MailMessageGenerator> mailMessageGenerators) {
        this.emailConfig = emailConfig;
        this.emailSender = emailSender;
        this.mailMessageGenerators = mailMessageGenerators.stream()
                .collect(Collectors.toMap(MailMessageGenerator::getMessageGeneratorTypeMapping, Function.identity()));
    }

    @Override
    public NotifierType getNotificationServiceTypeMapping() {
        return NotifierType.EMAIL;
    }

    // Notify user
    @Override
    public void notify(UserDetails userDetails, Notification notification, Contest contest) {
        String data = notification.getAsEmailNotification(mailMessageGenerators.get(notification.getType()));
        System.out.println("HERE -------------" + userDetails.getId() + userDetails.isSubscribed());
        if(userDetails.isSubscribed()) {
            sendEmail(userDetails.getEmail(), data, contest, notification.getType());
        }
    }

    // Notify channel
    @Override
    public void notify(Notification notification, Contest contest) {
        String data = notification.getAsEmailNotification(mailMessageGenerators.get(notification.getType()));
        contest.getSenseiEmails().forEach(email -> sendEmail(email, data, contest, notification.getType()));
    }

    public void notifyForSlackInvitation(String to, String message, Contest contest){
        sendEmail(to,message,contest, NotificationType.INVITATION);
    }

    private void sendEmail(String to, String data, Contest contest, NotificationType type) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);

            helper.setFrom(emailConfig.getUsername());
            helper.setTo(to);
            helper.setSubject(type.getMessage() + contest.getTitle());
            helper.setText(data, true);

            emailSender.send(message);

        } catch (MessagingException e) {
            LOGGER.warn("Email could not be sent: {}", e.getCause().getMessage());
        }
    }
}
