package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.messageGenerator.mail.MailMessageGenerator;
import com.dojo.notifications.service.messageGenerator.slack.SlackMessageGenerator;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;

public class SenseiNotification extends NotificationImpl {

    public SenseiNotification(UserDetailsService userDetailsService, Object content, String message, NotificationType type) {
        super(userDetailsService, content, message, type);
    }

    @Override
    public ChatPostMessageParams getAsSlackNotification(SlackMessageGenerator slackMessageGenerator, CustomSlackClient slackClient, String slackChannel) {
        return slackMessageGenerator.generateMessage(userDetailsService, super.getContextParams(), slackClient, slackChannel);
    }

    @Override
    public String getAsEmailNotification(MailMessageGenerator mailMessageGenerator) {
        return mailMessageGenerator.generateMessage(super.getContextParams());
    }
}
