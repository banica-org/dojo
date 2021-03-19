package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.slackNotifier.SlackMessageBuilder;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.models.blocks.objects.Text;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class LeaderboardNotification implements Notification {

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
    public ChatPostMessageParams.Builder convertToSlackNotification(SlackMessageBuilder slackMessageBuilder, CustomSlackClient slackClient) {
        return slackMessageBuilder.generateSlackContent(title, buildLeaderboardNames(slackClient), buildLeaderboardScores());
    }

    abstract Text buildLeaderboardNames(CustomSlackClient slackClient);

    abstract Text buildLeaderboardScores();

    public int getPositionAndIncrease() {
        return positionCounter++;
    }
}
