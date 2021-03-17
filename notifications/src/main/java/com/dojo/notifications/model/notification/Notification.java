package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.service.emailNotifier.MailContentBuilder;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;

import java.util.function.Function;

public interface Notification {

    ChatPostMessageParams.Builder convertToSlackNotification(Function<String, String> getChannelId, CustomSlackClient slackClient);

    String convertToEmailNotification(MailContentBuilder mailContentBuilder);
}
