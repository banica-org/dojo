package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.messageGenerator.mail.MailMessageGenerator;
import com.dojo.notifications.service.messageGenerator.slack.SlackMessageGenerator;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;

import java.util.Map;

public class ParticipantNotification extends NotificationImpl {

    private final UserDetails userDetails;

    public ParticipantNotification(UserDetailsService userDetailsService, Object content, UserDetails userDetails, String message, NotificationType type) {
        super(userDetailsService, content, message, type);
        this.userDetails = userDetails;
    }

    @Override
    public ChatPostMessageParams getAsSlackNotification(SlackMessageGenerator slackMessageGenerator, CustomSlackClient slackClient, String slackChannel) {
        Map<String, Object> contextParams = super.getContextParams();
        contextParams.put(USERDETAILS_KEY, userDetails);
        return slackMessageGenerator.generateMessage(userDetailsService, contextParams, slackClient, slackChannel);
    }

    @Override
    public String getAsEmailNotification(MailMessageGenerator mailMessageGenerator) {
        Map<String, Object> contextParams = super.getContextParams();
        contextParams.put(USERDETAILS_KEY, userDetails);
        return mailMessageGenerator.generateMessage(contextParams);
    }
}
