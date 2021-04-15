package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.service.messageGenerator.mail.MailMessageGenerator;
import com.dojo.notifications.service.messageGenerator.slack.SlackMessageGenerator;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;

public interface Notification {

    ChatPostMessageParams getAsSlackNotification(SlackMessageGenerator slackMessageGenerator, CustomSlackClient slackClient, String slackChannel);

    String getAsEmailNotification(MailMessageGenerator mailMessageGenerator);
}
