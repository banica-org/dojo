package com.dojo.notifications.model.user;

import lombok.Data;

@Data
public class UserDetails {
    private String id;
    private String email;
    private String slackEmail;
    private boolean subscribed;
}
