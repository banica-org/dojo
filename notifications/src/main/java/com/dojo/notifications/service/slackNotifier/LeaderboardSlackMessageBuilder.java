package com.dojo.notifications.service.slackNotifier;

import com.dojo.notifications.model.notification.SlackNotificationUtils;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.Attachment;
import com.hubspot.slack.client.models.actions.Action;
import com.hubspot.slack.client.models.actions.ActionType;
import com.hubspot.slack.client.models.blocks.Divider;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.springframework.stereotype.Component;

@Component
public class LeaderboardSlackMessageBuilder extends SlackMessageBuilder {

    private static final String BUTTON_TEXT = "View Leaderboard in Dojorena";
    private static final String BUTTON_STYLE = "primary";
    //TODO Change this to real url
    private static final String BUTTON_REDIRECT_URL = "http://localhost:8081/api/v1/codenjoy/leaderboard";

    public ChatPostMessageParams.Builder generateSlackContent(String title, Text leaderboardNames, Text leaderboardScores) {
        return ChatPostMessageParams.builder()
                .addBlocks(
                        Divider.builder().build(),
                        Section.of(Text.of(TextType.MARKDOWN, SlackNotificationUtils.makeBold(title)))
                                .withFields(
                                        Text.of(TextType.MARKDOWN, "*User*"),
                                        Text.of(TextType.MARKDOWN, "*Score*"),
                                        leaderboardNames,
                                        leaderboardScores))
                .addBlocks(Divider.builder().build())
                .addAttachments(Attachment.builder()
                        .addActions(Action.builder()
                                .setType(ActionType.BUTTON)
                                .setText(BUTTON_TEXT)
                                .setRawStyle(BUTTON_STYLE)
                                .setUrl(BUTTON_REDIRECT_URL)
                                .build())
                        .build());
    }
}
