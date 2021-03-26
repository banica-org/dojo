package com.dojo.notifications.model.contest.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public enum CommonNotificationsLevel {

    ON_ANY_LEADERBOARD_CHANGE(Arrays.asList(EventType.OTHER_LEADERBOARD_CHANGE, EventType.SCORE_CHANGES, EventType.POSITION_CHANGES)),
    ON_CHANGED_SCORE(Arrays.asList(EventType.SCORE_CHANGES, EventType.POSITION_CHANGES)),
    ON_CHANGED_POSITION(Collections.singletonList(EventType.POSITION_CHANGES)),
    NO_NOTIFICATIONS(Collections.emptyList());

    private final List<EventType> includedEventTypes;

    CommonNotificationsLevel(List<EventType> includedEventTypes) {
        this.includedEventTypes = includedEventTypes;
    }
}
