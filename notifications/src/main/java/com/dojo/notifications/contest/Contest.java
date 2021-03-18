package com.dojo.notifications.contest;

import com.dojo.notifications.contest.enums.CommonNotificationsLevel;
import com.dojo.notifications.contest.enums.NotifierType;
import lombok.Data;

import java.util.*;

@Data
public class Contest {

    private String contestId;
    private String title;
    private String slackToken;
    private String slackChannel;
    private List<String> senseiEmails;

    private Map<NotifierType, CommonNotificationsLevel> commonNotificationsLevel;
    private Set<NotifierType> personalNotifiers;

    public Contest() {
        senseiEmails = new LinkedList<>();
        commonNotificationsLevel = new HashMap<>();
        commonNotificationsLevel.put(NotifierType.SLACK, CommonNotificationsLevel.NO_NOTIFICATIONS);
        commonNotificationsLevel.put(NotifierType.EMAIL, CommonNotificationsLevel.NO_NOTIFICATIONS);
        personalNotifiers = new HashSet<>();
    }

    public void setSenseiEmailsAsString(String emails) {
        senseiEmails = Arrays.asList(emails.split(";"));
    }

    public String getSenseiEmailsAsString() {
        return String.join(";", senseiEmails);
    }

    public void setSlackCommonNotifications(CommonNotificationsLevel level) {
        commonNotificationsLevel.put(NotifierType.SLACK, level);
    }

    public CommonNotificationsLevel getSlackCommonNotifications() {
        return commonNotificationsLevel.get(NotifierType.SLACK);
    }

    public void setEmailCommonNotifications(CommonNotificationsLevel level) {
        commonNotificationsLevel.put(NotifierType.EMAIL, level);
    }

    public CommonNotificationsLevel getEmailCommonNotifications() {
        return commonNotificationsLevel.get(NotifierType.EMAIL);
    }

    public void setPersonalPositionChangeSlack(boolean personalPositionChangeSlack) {
        if (personalPositionChangeSlack) {
            personalNotifiers.add(NotifierType.SLACK);
        } else {
            personalNotifiers.remove(NotifierType.SLACK);
        }
    }

    public boolean getPersonalPositionChangeSlack() {
        return personalNotifiers.contains(NotifierType.SLACK);
    }

    public void setPersonalPositionChangeEmail(boolean personalPositionChangeEmail) {
        if (personalPositionChangeEmail) {
            personalNotifiers.add(NotifierType.EMAIL);
        } else {
            personalNotifiers.remove(NotifierType.EMAIL);
        }
    }

    public boolean getPersonalPositionChangeEmail() {
        return personalNotifiers.contains(NotifierType.EMAIL);
    }
}
