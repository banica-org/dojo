package com.dojo.notifications.model.request;

import lombok.Data;

@Data
public class SelectRequestModel {

    private String queryParameters;
    private String queryTable;
    private String querySpecification;
    private String receiver;
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

    public String getQueryTable() {
        return queryTable;
    }

    public void setQueryTable(String queryTable) {
        this.queryTable = queryTable;
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

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getReceivers() {
        return receivers;
    }
}
