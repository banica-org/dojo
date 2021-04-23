package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.docker.Container;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Divider;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ContainerSlackMessageGenerator extends SlackMessageGenerator {

    private static final String USERNAME = "Username: ";
    private static final String STATUS = "Status: ";

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.CONTAINER;
    }

    @Override
    public ChatPostMessageParams generateMessage(UserDetailsService userDetailsService, Map<String, Object> contextParams, CustomSlackClient slackClient, String slackChannel) {
        Container container = (Container) contextParams.get(CONTENT_KEY);
        String message = (String) contextParams.get(MESSAGE_KEY);
        return getChatPostMessageParams(container, slackChannel, message);
    }

    private ChatPostMessageParams getChatPostMessageParams(Container object, String slackChannel, String message) {

        ChatPostMessageParams.Builder builder = ChatPostMessageParams.builder()
                .addBlocks(Divider.builder().build())
                .addBlocks(Section.of(Text.of(TextType.MARKDOWN, message)))
                .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, USERNAME + object.getUsername())))
                .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, STATUS + object.getStatus())));

        return addLogs(builder, object.getLogs())
                .setChannelId(slackChannel)
                .build();
    }

    private ChatPostMessageParams.Builder addLogs(ChatPostMessageParams.Builder builder, List<String> logs) {
        logs.forEach(log -> builder.addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, log))));
        return builder;
    }
}
