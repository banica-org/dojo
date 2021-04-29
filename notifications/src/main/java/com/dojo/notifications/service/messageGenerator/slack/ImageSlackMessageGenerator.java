package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.docker.Image;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.springframework.stereotype.Service;

@Service
public class ImageSlackMessageGenerator extends SlackMessageGenerator {

    private static final String IMAGE_TAG = "Image tag: ";
    private static final String MESSAGE = "Message: ";

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.IMAGE;
    }

    @Override
    protected ChatPostMessageParams.Builder getChatPostMessageParams(Object object, String slackChannel, String message) {
        ChatPostMessageParams.Builder builder = super.getChatPostMessageParams(object, slackChannel, message);
        if (object instanceof Image) {
            Image image = (Image) object;
            builder
                    .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, IMAGE_TAG + image.getImageTag())))
                    .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, MESSAGE + image.getMessage())));
        }
        return builder;
    }
}
