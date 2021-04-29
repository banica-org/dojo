package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.docker.Container;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.springframework.stereotype.Service;

@Service
public class ContainerSlackMessageGenerator extends SlackMessageGenerator {

    private static final String USERNAME = "Username: ";
    private static final String STATUS = "Status: ";

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.CONTAINER;
    }

    @Override
    protected ChatPostMessageParams.Builder getChatPostMessageParams(Object object, String slackChannel, String message) {
        ChatPostMessageParams.Builder builder = super.getChatPostMessageParams(object, slackChannel, message);
        if (object instanceof Container) {
            Container container = (Container) object;
            builder
                    .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, USERNAME + container.getUsername())))
                    .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, STATUS + container.getStatus())));
            container.getLogs().forEach(log -> builder.addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, log))));
        }
        return builder;
    }
}
