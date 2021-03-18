package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.service.emailNotifier.MailContentBuilder;
import com.dojo.notifications.service.slackNotifier.SlackMessageBuilder;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;

public interface Notification {

    ChatPostMessageParams.Builder convertToSlackNotification(SlackMessageBuilder slackMessageBuilder, CustomSlackClient slackClient);

    String convertToEmailNotification(MailContentBuilder mailContentBuilder);
}
