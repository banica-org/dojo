package com.dojo.notifications.model.request;

import lombok.Data;

@Data
public class SelectRequestModel {

    private String queryParameters;
    private String querySpecification;
    private String describingMessage;
    private String eventType;
    private String communicationLevel;


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

    public String getCommunicationLevel(String eventType) {
        switch (eventType) {
            case "OTHER_LEADERBOARD_CHANGE": communicationLevel = "ON_ANY_LEADERBOARD_CHANGE";
                break;
            case "POSITION_CHANGES": communicationLevel = "ON_CHANGED_POSITION";
                break;
            case "SCORE_CHANGES": communicationLevel = "ON_CHANGED_SCORE";
                break;

            default: communicationLevel = "NO_NOTIFICATIONS";
        }
        return communicationLevel;
    }

    public void setCommonLevel(String commonLevel) {
        this.communicationLevel = commonLevel;
    }
}
