package com.dojo.notifications.model.request;

import lombok.Data;

@Data
public class SelectRequestModel {

    private String queryParameters;
    private String querySpecification;
    private String receiver;
    private String eventType;
    private String notificationLevel;
    private String describingMessage;
    private String notificationMessage;
    private String receivers;


    public SelectRequestModel() {
        this.queryParameters = "*";
    }

    public void setDescribingMessage(String describingMessage) {
        this.describingMessage = describingMessage;
    }

    public String getDescribingMessage() {
        return describingMessage;
    }

    public void setQuerySpecification(String querySpecification) {
        this.querySpecification = querySpecification;
    }

    public String getQuerySpecification() {
        return querySpecification;
    }

    public void setQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setNotificationLevel(String notificationLevel) {
        this.notificationLevel = notificationLevel;
    }

    public String getNotificationLevel(String eventType) {
        notificationLevel = determineCommunicationLevel(eventType);
        return notificationLevel;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getReceivers() {
        return receivers;
    }

    private String determineCommunicationLevel(String eventType) {
        switch (eventType) {
            case "OTHER_LEADERBOARD_CHANGE":
                return "ON_ANY_LEADERBOARD_CHANGE";
            case "POSITION_CHANGES":
                return "ON_CHANGED_POSITION";
            case "SCORE_CHANGES":
                return "ON_CHANGED_SCORE";

            default:
                return "NO_NOTIFICATIONS";
        }
    }
}
