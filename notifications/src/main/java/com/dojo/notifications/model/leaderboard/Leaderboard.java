package com.dojo.notifications.model.leaderboard;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.notification.SlackNotificationUtils;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Leaderboard {

    private static final UserDetails COMMON = null;

    private final List<Participant> participants;

    public Leaderboard(List<Participant> participants) {
        this.participants = participants;
    }

    public int getParticipantsCount() {
        return this.participants.size();
    }

    public String getUserIdByPosition(int position) {
        return this.participants.get(position).getUser().getId();
    }

    public long getScoreByPosition(int position) {
        return this.participants.get(position).getScore();
    }

    public Participant getParticipantByPosition(int position) {
        return this.participants.get(position);
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(this.participants);
    }

    public Text buildLeaderboardNames(UserDetails userDetails, UserDetailsService userDetailsService, CustomSlackClient slackClient) {
        AtomicInteger position = new AtomicInteger(1);
        StringBuilder names = new StringBuilder();

        participants.forEach(participant -> {
            String userId = slackClient.getSlackUserId(userDetailsService.getUserEmail(participant.getUser().getId()));
            String nameWithLink = "<slack://user?team=null&id=" + userId + "|" + participant.getUser().getName() + ">";
            String name = (userDetails != COMMON && participant.getUser().getId().equals(userDetails.getId())) ?
                    SlackNotificationUtils.makeBold(participant.getUser().getName()) : userId.isEmpty() ? participant.getUser().getName() : nameWithLink;
            names.append(SlackNotificationUtils.makeBold(position.getAndIncrement()))
                    .append(". ")
                    .append(name)
                    .append("\n");
        });
        return Text.of(TextType.MARKDOWN, String.valueOf(names));
    }

    public Text buildLeaderboardScores(UserDetails userDetails) {
        StringBuilder scores = new StringBuilder();

        participants.forEach(participant -> {
            String score = (userDetails != COMMON && participant.getUser().getId().equals(userDetails.getId())) ? SlackNotificationUtils.makeBold(participant.getScore())
                    : String.valueOf(participant.getScore());
            scores.append(score).append("\n");
        });
        return Text.of(TextType.MARKDOWN, String.valueOf(scores));
    }


    public List<Participant> sort() {
        return this.getParticipants()
                .stream()
                .sorted(new Comparator<Participant>() {
                    @Override
                    public int compare(Participant o1, Participant o2) {
                        return (int) (o2.getScore() - o1.getScore());
                    }
                }).collect(Collectors.toList());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Leaderboard)) return false;
        Leaderboard that = (Leaderboard) o;
        return participants.equals(that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participants);
    }
}
