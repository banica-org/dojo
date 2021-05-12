package com.dojo.notifications.model.contest;

import com.dojo.notifications.model.contest.enums.NotifierType;
import lombok.Data;

import java.util.*;

@Data
public class Contest {

    private String contestId;
    private String title;
    private String slackToken;
    private String slackChannel;
    private Set<String> senseiEmails;

    private Set<NotifierType> notifiers;

    public Contest() {
        senseiEmails = new HashSet<>();
        notifiers = new HashSet<>();
    }

    public void setSenseiEmailsAsString(String emails) {
        senseiEmails = new HashSet<>(Arrays.asList(emails.split(";")));
    }

    public String getSenseiEmailsAsString() {
        return String.join(";", senseiEmails);
    }

    public void setSlackNotifier(boolean slackNotifier) {
        if (slackNotifier) {
            notifiers.add(NotifierType.SLACK);
        } else {
            notifiers.remove(NotifierType.SLACK);
        }
    }

    public boolean getSlackNotifier() {
        return notifiers.contains(NotifierType.SLACK);
    }

    public void setEmailNotifier(boolean emailNotifier) {
        if (emailNotifier) {
            notifiers.add(NotifierType.EMAIL);
        } else {
            notifiers.remove(NotifierType.EMAIL);
        }
    }

    public boolean getEmailNotifier() {
        return notifiers.contains(NotifierType.EMAIL);
    }
}
