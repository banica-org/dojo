package com.dojo.notifications.service.notificationService;

import com.dojo.notifications.configuration.EmailConfig;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.notification.Notification;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.messageGenerator.mail.MailMessageGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
public class EmailNotificationServiceTest {

    private final String CONVERTED_STRING_FOR_NOTIFICATIONS = "converted";
    private final String EMAIL_FOR_USER = "xxxx@gmail.com";
    private final String USERNAME_FOR_EMAILCONFIG = "Pesho";

    @Mock
    private UserDetails userDetails;

    @Mock
    private Notification notification;

    @Mock
    private Contest contest;

    @Mock
    private MailMessageGenerator mailMessageGenerator;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private EmailConfig emailConfig;

    @Mock
    private JavaMailSender emailSender;

    private EmailNotificationService emailNotificationService;

    @Before
    public void init() {
        when(mailMessageGenerator.getMessageGeneratorTypeMapping()).thenReturn(NotificationType.LEADERBOARD);
        when(notification.getType()).thenReturn(NotificationType.LEADERBOARD);
        emailNotificationService = new EmailNotificationService(emailConfig, emailSender, Collections.singletonList(mailMessageGenerator));
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
        when(notification.getAsEmailNotification(mailMessageGenerator)).thenReturn(CONVERTED_STRING_FOR_NOTIFICATIONS);
        when(userDetails.getEmail()).thenReturn(EMAIL_FOR_USER);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailConfig.getUsername()).thenReturn(USERNAME_FOR_EMAILCONFIG);


        //Act
        emailNotificationService.notify(userDetails, notification, contest);

        //Assert
        verify(notification, times(1)).getAsEmailNotification(mailMessageGenerator);
        verify(userDetails, times(1)).getEmail();
        verify(emailSender, times(1)).createMimeMessage();
        verify(emailConfig, times(1)).getUsername();
    }

    @Test
    public void notifyChannelTest() {
        //Arrange
        Set<String> emails = new HashSet<>();
        emails.add(EMAIL_FOR_USER);
        int size = emails.size();

        when(notification.getAsEmailNotification(mailMessageGenerator)).thenReturn(CONVERTED_STRING_FOR_NOTIFICATIONS);
        when(contest.getSenseiEmails()).thenReturn(emails);
        when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailConfig.getUsername()).thenReturn(USERNAME_FOR_EMAILCONFIG);

        //Act
        emailNotificationService.notify(notification, contest);

        //Assert
        verify(notification, times(1)).getAsEmailNotification(mailMessageGenerator);
        verify(contest, times(1)).getSenseiEmails();
        verify(emailSender, times(size)).createMimeMessage();
        verify(emailConfig, times(size)).getUsername();
    }
}
