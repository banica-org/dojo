package com.dojo.notifications.model.request;

import lombok.Data;

@Data
public class SelectRequestModel {

    private String queryParameters;
    private String querySpecification;
    private String describingMessage;
    private String eventType;


    public SelectRequestModel(){
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
}
