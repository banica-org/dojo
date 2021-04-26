package com.dojo.notifications.model.leaderboard;

import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.notification.SlackNotificationUtils;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import com.dojo.notifications.service.UserDetailsService;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Leaderboard {

    private static final UserDetails COMMON = null;

    private final Set<Participant> participants;
    private final Set<String> participantsNames;

    public Leaderboard(Set<Participant> participants) {
        this.participants = participants;
        this.participantsNames = new HashSet<>();
        participants.forEach(participant -> participantsNames.add(participant.getUser().getName()));
    }

    public int getParticipantsCount() {
        return this.participants.size();
    }

    public int getPositionByUserId(String userId) {
        int pos = 1;
        for (Participant p : participants) {
            if (p.getUser().getId().equals(userId)) {
                return pos;
            }
            pos++;
        }
        return participants.size() + 1;
    }

    public long getScoreByUserId(String userId) {
        for (Participant p : participants) {
            if (p.getUser().getId().equals(userId)) {
                return p.getScore();
            }
        }
        return 0;
    }

    public String getUserIdByPosition(int position) {
        for (Participant p : participants) {
            if (position == 0) {
                return p.getUser().getId();
            }
            position--;
        }
        return "No such position";
    }

    public long getScoreByPosition(int position) {
        for (Participant p : participants) {
            if (position == 0) {
                return p.getScore();
            }
            position--;
        }
        return -1;
    }

    public Set<Participant> getParticipants() {
        return this.participants;
    }

    public Text buildLeaderboardNames(UserDetails userDetails, UserDetailsService userDetailsService, CustomSlackClient slackClient) {
        AtomicInteger position = new AtomicInteger(1);
        StringBuilder names = new StringBuilder();

        getParticipants().forEach(participant -> {
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

        getParticipants().forEach(participant -> {
            String score = (userDetails != COMMON && participant.getUser().getId().equals(userDetails.getId())) ? SlackNotificationUtils.makeBold(participant.getScore())
                    : String.valueOf(participant.getScore());
            scores.append(score).append("\n");
        });
        return Text.of(TextType.MARKDOWN, String.valueOf(scores));
    }

    public void updateParticipant(Participant updatedParticipant) {
        String participantName = updatedParticipant.getUser().getName();

        if (participantsNames.contains(participantName)) {
            UserInfo updatedUser = updatedParticipant.getUser();
            for (Participant participant : this.participants) {
                if (participant.getUser().equals(updatedUser)) {
                    this.participants.remove(participant);
                    break;
                }
            }
        } else {
            this.participantsNames.add(participantName);
        }

        this.participants.add(updatedParticipant);
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
