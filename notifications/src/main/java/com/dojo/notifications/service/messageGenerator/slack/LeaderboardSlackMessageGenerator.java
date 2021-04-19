package com.dojo.notifications.service.messageGenerator.slack;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.SlackNotificationUtils;
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

@Service
public class LeaderboardSlackMessageGenerator extends SlackMessageGenerator {

    private static final String PERSONAL_TITLE = "Your have leaderboard changes";
    private static final String COMMON_TITLE = "Leaderboard update";

    private static final String BUTTON_TEXT = "View Leaderboard in Dojorena";
    private static final String BUTTON_STYLE = "primary";
    //TODO Change this to real url and add event id
    private static final String BUTTON_REDIRECT_URL = "http://localhost:8081/api/v1/codenjoy/leaderboard";
    private static final String USER = "*User*";
    private static final String SCORE = "*Score*";

    private static final UserDetails COMMON = null;

    @Override
    public ChatPostMessageParams generateMessage(UserDetailsService userDetailsService, UserDetails userDetails, Leaderboard leaderboard, CustomSlackClient slackClient, String slackChannel, String queryMessage) {
        Text leaderboardNames = leaderboard.buildLeaderboardNames(userDetails, userDetailsService, slackClient);
        Text leaderboardScores = leaderboard.buildLeaderboardScores(userDetails);

        return getChatPostMessageParams(slackChannel, leaderboardNames, leaderboardScores, PERSONAL_TITLE, queryMessage);
    }

    @Override
    public ChatPostMessageParams generateMessage(UserDetailsService userDetailsService, Leaderboard leaderboard, CustomSlackClient slackClient, String slackChannel, String queryMessage) {
        Text leaderboardNames = leaderboard.buildLeaderboardNames(COMMON, userDetailsService, slackClient);
        Text leaderboardScores = leaderboard.buildLeaderboardScores(COMMON);

        return getChatPostMessageParams(slackChannel, leaderboardNames, leaderboardScores, COMMON_TITLE, queryMessage);
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
