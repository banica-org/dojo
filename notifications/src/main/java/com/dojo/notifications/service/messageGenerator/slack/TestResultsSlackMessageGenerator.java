package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.docker.TestResults;
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
public class TestResultsSlackMessageGenerator extends SlackMessageGenerator {

    private static final String USERNAME = "Username: ";
    private static final String TEST_RESULTS = "Failed test cases: ";

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.TEST_RESULTS;
    }

    @Override
    public ChatPostMessageParams generateMessage(UserDetailsService userDetailsService, Map<String, Object> contextParams, CustomSlackClient slackClient, String slackChannel) {
        TestResults testResults = (TestResults) contextParams.get(CONTENT_KEY);
        String message = (String) contextParams.get(MESSAGE_KEY);
        return getChatPostMessageParams(testResults, slackChannel, message);
    }

    private ChatPostMessageParams getChatPostMessageParams(TestResults object, String slackChannel, String message) {

        ChatPostMessageParams.Builder builder = ChatPostMessageParams.builder()
                .addBlocks(Divider.builder().build())
                .addBlocks(Section.of(Text.of(TextType.MARKDOWN, message)))
                .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, USERNAME + object.getUsername())))
                .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, TEST_RESULTS)));

        return addTestResults(builder, object.getFailedTestCases())
                .setChannelId(slackChannel)
                .build();
    }

    private ChatPostMessageParams.Builder addTestResults(ChatPostMessageParams.Builder builder, Map<String, String> testResults) {
        testResults.forEach((methodName, expected) -> builder.addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, methodName + ": " + expected))));
        return builder;
    }
}
