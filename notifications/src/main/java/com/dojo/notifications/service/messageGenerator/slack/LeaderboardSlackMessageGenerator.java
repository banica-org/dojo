package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.leaderboard.SlackNotificationUtils;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.Attachment;
import com.hubspot.slack.client.models.actions.Action;
import com.hubspot.slack.client.models.actions.ActionType;
import com.hubspot.slack.client.models.blocks.Divider;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LeaderboardSlackMessageGenerator extends SlackMessageGenerator {

    private static final String PERSONAL_TITLE = "Your position in leaderboard has changed";
    private static final String COMMON_TITLE = "Leaderboard update";

    private static final String BUTTON_TEXT = "View Leaderboard in Dojorena";
    private static final String BUTTON_STYLE = "primary";
    //TODO Change this to real url and add event id
    private static final String BUTTON_REDIRECT_URL = "http://localhost:8081/api/v1/codenjoy/leaderboard";
    private static final String USER = "*User*";
    private static final String SCORE = "*Score*";

    @Override
    public NotificationType getMessageGeneratorTypeMapping() {
        return NotificationType.LEADERBOARD;
    }

    @Override
    public ChatPostMessageParams generateMessage(UserDetailsService userDetailsService, Map<String, Object> contextParams, CustomSlackClient slackClient, String slackChannel) {
        Leaderboard leaderboard = (Leaderboard) contextParams.get(CONTENT_KEY);
        UserDetails userDetails;
        String title;

        if (contextParams.containsKey(USERDETAILS_KEY)) {
            userDetails = (UserDetails) contextParams.get(USERDETAILS_KEY);
            title = PERSONAL_TITLE;
        } else {
            userDetails = null;
            title = COMMON_TITLE;
        }

        String message = (String) contextParams.get(MESSAGE_KEY);
        Text leaderboardNames = leaderboard.buildLeaderboardNames(userDetails, userDetailsService, slackClient);
        Text leaderboardScores = leaderboard.buildLeaderboardScores(userDetails);

        return getChatPostMessageParams(slackChannel, leaderboardNames, leaderboardScores, title, message);
    }

    private ChatPostMessageParams getChatPostMessageParams(String slackChannel, Text leaderboardNames, Text leaderboardScores, String personalTitle, String queryMessage) {
        ChatPostMessageParams.Builder builder = ChatPostMessageParams.builder();
        addDivider(builder);
        addMessage(builder, queryMessage);
        addUsersWithScores(builder, personalTitle, leaderboardNames, leaderboardScores);
        addDivider(builder);
        addRedirectButton(builder);
        builder.setChannelId(slackChannel);
        return builder.build();
    }

    private void addDivider(ChatPostMessageParams.Builder builder) {
        builder.addBlocks(Divider.builder().build());
    }

    private void addMessage(ChatPostMessageParams.Builder builder, String queryMessage) {
        builder.addBlocks(Section.of(Text.of(TextType.PLAIN_TEXT, queryMessage)));
    }

    private void addUsersWithScores(ChatPostMessageParams.Builder builder, String title, Text leaderboardNames, Text leaderboardScores) {
        builder.addBlocks(Section.of(Text.of(TextType.MARKDOWN, SlackNotificationUtils.makeBold(title)))
                .withFields(
                        Text.of(TextType.MARKDOWN, USER),
                        Text.of(TextType.MARKDOWN, SCORE),
                        leaderboardNames,
                        leaderboardScores));
    }

    private void addRedirectButton(ChatPostMessageParams.Builder builder) {
        builder.addAttachments(Attachment.builder()
                .addActions(Action.builder()
                        .setType(ActionType.BUTTON)
                        .setText(BUTTON_TEXT)
                        .setRawStyle(BUTTON_STYLE)
                        .setUrl(BUTTON_REDIRECT_URL)
                        .build())
                .build());
    }
}
