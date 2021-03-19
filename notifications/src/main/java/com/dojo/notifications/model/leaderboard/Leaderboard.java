package com.dojo.notifications.model.leaderboard;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.notification.SlackNotificationUtils;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Leaderboard {

    private static final UserDetails COMMON = null;

    @Autowired
    private UserDetailsService userDetailsService;
    private List<User> leaderboard;

    public Leaderboard(List<User> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public Text buildLeaderboardNames(UserDetails userDetails, CustomSlackClient slackClient) {
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

    public Text buildLeaderboardScores(UserDetails userDetails) {
        StringBuilder scores = new StringBuilder();

        leaderboard.forEach(user -> {
            String score = (userDetails != COMMON && user.getUser().getId() == userDetails.getId()) ? SlackNotificationUtils.makeBold(user.getScore())
                    : String.valueOf(user.getScore());
            scores.append(score).append("\n");
        });
        return Text.of(TextType.MARKDOWN, String.valueOf(scores));
    }
}
