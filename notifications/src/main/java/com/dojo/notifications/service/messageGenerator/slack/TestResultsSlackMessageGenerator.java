package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.docker.TestResults;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.springframework.stereotype.Service;

@Service
public class TestResultsSlackMessageGenerator extends SlackMessageGenerator {

    private static final String USERNAME = "Username: ";
    private static final String TEST_RESULTS = "Failed test cases: ";

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.TEST_RESULTS;
    }

    @Override
    protected ChatPostMessageParams.Builder getChatPostMessageParams(Object object, String slackChannel, String message) {
        ChatPostMessageParams.Builder builder = super.getChatPostMessageParams(object, slackChannel, message);
        if (object instanceof TestResults) {
            TestResults testResults = (TestResults) object;
            builder
                    .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, USERNAME + testResults.getUsername())))
                    .addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, TEST_RESULTS)));
            testResults.getFailedTestCases().forEach((methodName, expected) -> builder.addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, methodName + ": " + expected))));
        }
        return builder;
    }
}
