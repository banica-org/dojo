package com.dojo.notifications.service.emailNotifier;

import com.dojo.notifications.configuration.EmailConfig;
import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.contest.enums.NotifierType;
import com.dojo.notifications.model.notification.Notification;
import com.dojo.notifications.model.user.UserDetails;
import org.junit.Assert;
import org.junit.Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class EmailNotificationServiceTest {

    @Mock
    private UserDetails userDetails;

    @Mock
    private Notification notification;

    @Mock
    private Contest contest;

    @Mock
    private MailContentBuilder mailContentBuilder;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private EmailConfig emailConfig;

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @BeforeEach
    public void init() {
        emailNotificationService = new EmailNotificationService();
    }

    @Test
    public void getNotificationServiceTypeMappingTest() {
        //Arrange
        NotifierType expected = NotifierType.EMAIL;

        //Act
        NotifierType actual = emailNotificationService.getNotificationServiceTypeMapping();

        //Assert
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void notifyUserTest() {
        //Arrange
        when(notification.convertToEmailNotification(mailContentBuilder)).thenReturn("converted");
        when(userDetails.getEmail()).thenReturn("xxx@gmail.com");
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailConfig.getUsername()).thenReturn("Pesho");

        //Act
        emailNotificationService.notify(userDetails, notification, contest);

        //Assert
        verify(notification, times(1)).convertToEmailNotification(mailContentBuilder);
        verify(userDetails, times(1)).getEmail();
        verify(emailSender, times(1)).createMimeMessage();
        verify(emailConfig, times(1)).getUsername();
    }

    @Test
    public void notifyChannelTest() {
        //Arrange
        List<String> emails = new LinkedList<>();
        emails.add("email1");
        when(notification.convertToEmailNotification(mailContentBuilder)).thenReturn("converted");
        when(contest.getSenseiEmails()).thenReturn(emails);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailConfig.getUsername()).thenReturn("Pesho");

        //Act
        emailNotificationService.notify(notification, contest);

        //Assert
        verify(notification, times(1)).convertToEmailNotification(mailContentBuilder);
        verify(contest, times(1)).getSenseiEmails();
        verify(emailSender, times(1)).createMimeMessage();
        verify(emailConfig, times(1)).getUsername();
    }
}
