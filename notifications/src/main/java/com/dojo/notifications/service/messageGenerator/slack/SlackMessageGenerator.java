package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Divider;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public abstract class SlackMessageGenerator {

    protected static final String USERDETAILS_KEY = "userDetails";
    protected static final String CONTENT_KEY = "content";
    protected static final String MESSAGE_KEY = "message";

    public abstract NotificationType getMessageGeneratorTypeMapping();

    public ChatPostMessageParams generateMessage(UserDetailsService userDetailsService, Map<String, Object> contextParams, CustomSlackClient slackClient, String slackChannel) {
        String message = (String) contextParams.get(MESSAGE_KEY);
        return getChatPostMessageParams(contextParams.get(CONTENT_KEY), slackChannel, message).build();
    }

    protected ChatPostMessageParams.Builder getChatPostMessageParams(Object object, String slackChannel, String message) {
        //        MarkdownToHTML
        //        message = message.replace("~~","~");

        //HTML to MarkDown
        MutableDataSet options = new MutableDataSet();
        String markdown = FlexmarkHtmlConverter.builder(options).build().convert(message);
        markdown = markdown.replace("*", "_");
        markdown = markdown.replace("__", "*");

        return ChatPostMessageParams.builder()
                .setChannelId(slackChannel)
                .addBlocks(Divider.builder().build())
                .addBlocks(Section.of(Text.of(TextType.MARKDOWN, markdown)));
    }
}
