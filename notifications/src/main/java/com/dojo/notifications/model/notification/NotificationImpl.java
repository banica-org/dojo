package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.UserDetailsService;

import java.util.HashMap;
import java.util.Map;

public abstract class NotificationImpl implements Notification {

    private static final String MESSAGE_KEY = "message";
    private static final String CONTENT_KEY = "content";
    protected static final String USERDETAILS_KEY = "userDetails";

    protected final UserDetailsService userDetailsService;
    protected final Object content;
    protected final String message;
    protected final NotificationType type;

    public NotificationImpl(UserDetailsService userDetailsService, Object content, String message, NotificationType type) {
        this.userDetailsService = userDetailsService;
        this.content = content;
        this.message = message;
        this.type = type;
    }

    @Override
    public NotificationType getType() {
        return type;
    }

    protected Map<String, Object> getContextParams() {
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put(MESSAGE_KEY, message);
        contextParams.put(CONTENT_KEY, content);
        return contextParams;
    }
}
