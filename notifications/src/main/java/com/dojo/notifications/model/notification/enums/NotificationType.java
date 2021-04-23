package com.dojo.notifications.model.notification.enums;

public enum NotificationType {
    LEADERBOARD("Leaderboard change for "),
    IMAGE("Image build for "),
    CONTAINER("Container run for "),
    TEST_RESULTS("Test results for ");

    private String message;

    NotificationType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
