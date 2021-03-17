package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.Attachment;
import com.hubspot.slack.client.models.actions.Action;
import com.hubspot.slack.client.models.actions.ActionType;
import com.hubspot.slack.client.models.blocks.Divider;
import com.hubspot.slack.client.models.blocks.Section;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Getter
public abstract class LeaderboardNotification implements Notification {

    public static final String BUTTON_TEXT = "View Leaderboard in Dojorena";
    public static final String BUTTON_STYLE = "primary";
    //TODO Change this to real url
    public static final String BUTTON_REDIRECT_URL = "http://localhost:8081/api/v1/codenjoy/leaderboard";

    private final List<User> leaderboard;

    private final String title;

    private int positionCounter = 1;

    private final UserDetailsService userDetailsService;

    public LeaderboardNotification(List<User> leaderboard, UserDetailsService userDetailsService, String title) {
        this.leaderboard = leaderboard;
        this.userDetailsService = userDetailsService;
        this.title = title;
    }

    @Override
    public ChatPostMessageParams.Builder convertToSlackNotification(Function<String, String> getSlackUserId, CustomSlackClient slackClient) {
        return ChatPostMessageParams.builder()
                .addBlocks(
                        Divider.builder().build(),
                        Section.of(Text.of(TextType.MARKDOWN, SlackNotificationUtils.makeBold(title)))
                                .withFields(
                                        Text.of(TextType.MARKDOWN, "*User*"),
                                        Text.of(TextType.MARKDOWN, "*Score*"),
                                        buildLeaderboardNames(getSlackUserId, slackClient),
                                        buildLeaderboardScores()))
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

    abstract Text buildLeaderboardNames(Function<String, String> getSlackUserId, CustomSlackClient slackClient);

    abstract Text buildLeaderboardScores();

    public int getPositionAndIncrease() {
        return positionCounter++;
    }
}
