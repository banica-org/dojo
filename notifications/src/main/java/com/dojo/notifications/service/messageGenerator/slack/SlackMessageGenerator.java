package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public abstract class SlackMessageGenerator {

    protected static final String USERDETAILS_KEY = "userDetails";
    protected static final String CONTENT_KEY = "content";
    protected static final String MESSAGE_KEY = "message";

    public abstract NotificationType getMessageGeneratorTypeMapping();

    public abstract ChatPostMessageParams generateMessage(UserDetailsService userDetailsService, Map<String, Object> contextParams, CustomSlackClient slackClient, String slackChannel);
}
