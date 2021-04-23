package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.docker.Image;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Divider;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ImageSlackMessageGenerator extends SlackMessageGenerator {

    private static final String IMAGE_TAG = "Image tag: ";
    private static final String MESSAGE = "Message: ";

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.IMAGE;
    }

    @Override
    public ChatPostMessageParams generateMessage(UserDetailsService userDetailsService, Map<String, Object> contextParams, CustomSlackClient slackClient, String slackChannel) {
        Image image = (Image) contextParams.get(CONTENT_KEY);
        String message = (String) contextParams.get(MESSAGE_KEY);
        return getChatPostMessageParams(image, slackChannel, message);
    }

    private ChatPostMessageParams getChatPostMessageParams(Image object, String slackChannel, String message) {

        return ChatPostMessageParams.builder()
                .addBlocks(Divider.builder().build())
                .addBlocks(Section.of(Text.of(TextType.MARKDOWN, message)))
                .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, IMAGE_TAG + object.getImageTag())))
                .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, MESSAGE + object.getMessage())))
                .setChannelId(slackChannel)
                .build();
    }
}
