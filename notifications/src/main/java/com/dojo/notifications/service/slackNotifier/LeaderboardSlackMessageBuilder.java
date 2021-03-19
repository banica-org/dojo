package com.dojo.notifications.service.slackNotifier;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.notification.SlackNotificationUtils;
import com.dojo.notifications.model.user.User;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LeaderboardSlackMessageBuilder extends SlackMessageBuilder {

    private static final String PERSONAL_TITLE = "Your position in leaderboard has changed";
    private static final String COMMON_TITLE = "Leaderboard update";

    private static final String BUTTON_TEXT = "View Leaderboard in Dojorena";
    private static final String BUTTON_STYLE = "primary";
    //TODO Change this to real url
    private static final String BUTTON_REDIRECT_URL = "http://localhost:8081/api/v1/codenjoy/leaderboard";
    private static final String USER = "*User*";
    private static final String SCORE = "*Score*";

    private static final UserDetails COMMON = null;

    @Autowired
    private UserDetailsService userDetailsService;

    public LeaderboardSlackMessageBuilder() {
    }

    @Override
    public ChatPostMessageParams generateSlackContent(UserDetails userDetails, List<User> leaderboard, CustomSlackClient slackClient, String slackChannel) {
        Text leaderboardNames = buildLeaderboardNames(userDetails, leaderboard, slackClient);
        Text leaderboardScores = buildLeaderboardScores(userDetails, leaderboard);

        return getChatPostMessageParams(slackChannel, leaderboardNames, leaderboardScores, PERSONAL_TITLE);
    }

    @Override
    public ChatPostMessageParams generateSlackContent(List<User> leaderboard, CustomSlackClient slackClient, String slackChannel) {
        Text leaderboardNames = buildLeaderboardNames(COMMON, leaderboard, slackClient);
        Text leaderboardScores = buildLeaderboardScores(COMMON, leaderboard);

        return getChatPostMessageParams(slackChannel, leaderboardNames, leaderboardScores, COMMON_TITLE);
    }

    private ChatPostMessageParams getChatPostMessageParams(String slackChannel, Text leaderboardNames, Text leaderboardScores, String personalTitle) {
        ChatPostMessageParams.Builder builder = ChatPostMessageParams.builder();
        addDivider(builder);
        addUsersWithScores(builder, personalTitle, leaderboardNames, leaderboardScores);
        addDivider(builder);
        addRedirectButton(builder);
        builder.setChannelId(slackChannel);
        return builder.build();
    }

    private Text buildLeaderboardNames(UserDetails userDetails, List<User> leaderboard, CustomSlackClient slackClient) {
        AtomicInteger position = new AtomicInteger(1);
        StringBuilder names = new StringBuilder();

        leaderboard.forEach(user -> {
            String userId = slackClient.getSlackUserId(userDetailsService.getUserEmail(user.getUser().getId()));
            String nameWithLink = "<slack://user?team=null&id=" + userId + "|" + user.getUser().getName() + ">";
            String name = (userDetails != COMMON && user.getUser().getId() == userDetails.getId()) ?
                    SlackNotificationUtils.makeBold(user.getUser().getName()) : userId.isEmpty() ? user.getUser().getName() : nameWithLink;
            names.append(SlackNotificationUtils.makeBold(position.getAndIncrement()))
                    .append(". ")
                    .append(name)
                    .append("\n");
        });
        return Text.of(TextType.MARKDOWN, String.valueOf(names));
    }

    private Text buildLeaderboardScores(UserDetails userDetails, List<User> leaderboard) {
        StringBuilder scores = new StringBuilder();

        leaderboard.forEach(user -> {
            String score = (userDetails != COMMON && user.getUser().getId() == userDetails.getId()) ? SlackNotificationUtils.makeBold(user.getScore())
                    : String.valueOf(user.getScore());
            scores.append(score).append("\n");
        });
        return Text.of(TextType.MARKDOWN, String.valueOf(scores));
    }

    private void addDivider(ChatPostMessageParams.Builder builder) {
        builder.addBlocks(Divider.builder().build());
    }

    private void addUsersWithScores(ChatPostMessageParams.Builder builder, String title, Text leaderboardNames, Text leaderboardScores) {
        builder.addBlocks(
                Section.of(Text.of(TextType.MARKDOWN, SlackNotificationUtils.makeBold(title)))
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
