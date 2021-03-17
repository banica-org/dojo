package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.emailNotifier.MailContentBuilder;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PersonalLeaderboardNotification extends LeaderboardNotification {

    private final UserDetails userDetails;

    public PersonalLeaderboardNotification(List<User> leaderboard, UserDetailsService userDetailsService, UserDetails userDetails) {
        super(leaderboard, userDetailsService, "Your position in leaderboard has changed");
        this.userDetails = userDetails;
    }

    @Override
    public final Text buildLeaderboardNames(Function<String, String> getSlackUserId, CustomSlackClient slackClient) {
        StringBuilder names = new StringBuilder();

        getLeaderboard().forEach(user -> {
            String userId = getSlackUserId.apply(getUserDetailsService().getUserEmail(user.getUser().getId()));
            String nameWithLink = "<slack://user?team=null&id=" + userId + "|" + user.getUser().getName() + ">";
            String name = (user.getUser().getId() == userDetails.getId()) ?
                    SlackNotificationUtils.makeBold(user.getUser().getName()) : userId.isEmpty() ? user.getUser().getName() : nameWithLink;
            names.append(SlackNotificationUtils.makeBold(getPositionAndIncrease()))
                    .append(". ")
                    .append(name)
                    .append("\n");
        });
        return Text.of(TextType.MARKDOWN, String.valueOf(names));
    }

    @Override
    public Text buildLeaderboardScores() {
        StringBuilder scores = new StringBuilder();

        getLeaderboard().forEach(user -> {
            String score = (user.getUser().getId() == userDetails.getId()) ? SlackNotificationUtils.makeBold(user.getScore())
                    : String.valueOf(user.getScore());
            scores.append(score).append("\n");
        });
        return Text.of(TextType.MARKDOWN, String.valueOf(scores));
    }

    @Override
    public String convertToEmailNotification(MailContentBuilder mailContentBuilder) {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put("leaderboard", getLeaderboard());
        contextParams.put("userDetails", userDetails);
        return mailContentBuilder.generateMailContent(contextParams);
    }
}
