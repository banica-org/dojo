package com.dojo.notifications.contest;

import com.dojo.notifications.contest.enums.CommonNotificationsLevel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class ContestTest {

    private static final String EMAIL = "sensei@email.com";

    private Contest contest;
    private List<String> senseiEmails;

    @Before
    public void init() {
        contest = new Contest();
        senseiEmails = new ArrayList<>();
        senseiEmails.add(EMAIL);
        senseiEmails.add(EMAIL);
    }

    @Test
    public void getSenseiEmailsAsStringTest() {
        contest.setSenseiEmails(senseiEmails);
        String expected = String.join(";", senseiEmails);

        String actual = contest.getSenseiEmailsAsString();

        assertEquals(expected, actual);
    }

    @Test
    public void setSenseiEmailsAsStringTest() {
        contest.setSenseiEmailsAsString(String.join(";", senseiEmails));

        List<String> actual = contest.getSenseiEmails();

        assertEquals(senseiEmails, actual);
    }

    @Test
    public void slackCommonNotificationsTest() {
        contest.setSlackCommonNotifications(CommonNotificationsLevel.ON_CHANGED_POSITION);

        assertEquals(CommonNotificationsLevel.ON_CHANGED_POSITION, contest.getSlackCommonNotifications());
    }

    @Test
    public void emailCommonNotificationsTest() {
        contest.setEmailCommonNotifications(CommonNotificationsLevel.ON_CHANGED_POSITION);

        assertEquals(CommonNotificationsLevel.ON_CHANGED_POSITION, contest.getEmailCommonNotifications());
    }

    @Test
    public void personalPositionChangeSlackTest() {
        contest.setPersonalPositionChangeSlack(true);

        assertTrue(contest.getPersonalPositionChangeSlack());
    }

    @Test
    public void personalPositionChangeEmailTest() {
        contest.setPersonalPositionChangeEmail(true);

        assertTrue(contest.getPersonalPositionChangeEmail());
    }

    @Test
    public void personalPositionChangeFalseSlackTest() {
        contest.setPersonalPositionChangeSlack(false);

        assertFalse(contest.getPersonalPositionChangeSlack());
    }

    @Test
    public void personalPositionChangeFalseEmailTest() {
        contest.setPersonalPositionChangeEmail(false);

        assertFalse(contest.getPersonalPositionChangeEmail());
    }

}
