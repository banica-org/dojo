package com.dojo.notifications.model.request;

import lombok.Data;

@Data
public class SelectRequestModel {

    private String queryParameters;
    private String querySpecification;
    private String describingMessage;
    private String eventType;
    private String communicationLevel;
    private String receiver;


    public SelectRequestModel() {
        this.queryParameters = "*";
        this.querySpecification = "";
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

    public String getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getQuerySpecification() {
        return querySpecification;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    private String determineCommunicationLevel() {
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

    public String getCommunicationLevel(String eventType) {
        communicationLevel = determineCommunicationLevel();
        return communicationLevel;
    }

    public void setCommonLevel(String commonLevel) {
        this.communicationLevel = commonLevel;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
