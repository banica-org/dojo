package com.dojo.notifications.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SELECT_REQUESTS")
public class SelectRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    int id;

    @Column(name = "QUERY")
    String query;

    @Column(name = "RECEIVER")
    String receiver;

    @Column(name = "EVENT_TYPE")
    String eventType;

    @Column(name = "NOTIFICATION_LEVEL")
    String notificationLevel;

    @Column(name = "QUERY_DESCRIPTION")
    String queryDescription;

    @Column(name = "MESSAGE")
    String message;

}
