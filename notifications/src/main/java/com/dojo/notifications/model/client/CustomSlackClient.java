package com.dojo.notifications.model.client;

import com.hubspot.slack.client.SlackClient;

public interface CustomSlackClient extends SlackClient {
    String getConversationId(String userEmail);
}
