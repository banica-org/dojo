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
    int ID;

    @Column(name = "QUERY")
    String QUERY;

    @Column(name = "EVENT_TYPE")
    String EVENT_TYPE;

    @Column(name = "MESSAGE")
    String MESSAGE;

    @Column(name = "QUANTIFIER")
    String QUANTIFIER;
}
