package com.dojo.notifications.model.contest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class ContestTest {

    private static final String EMAIL = "sensei@email.com";
    private static final String OTHER_EMAIL = "sensei1@email.com";

    private Contest contest;
    private Set<String> senseiEmails;

    @Before
    public void init() {
        contest = new Contest();
        senseiEmails = new HashSet<>();
        senseiEmails.add(EMAIL);
        senseiEmails.add(OTHER_EMAIL);
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

        Set<String> actual = contest.getSenseiEmails();

        assertEquals(senseiEmails, actual);
    }

    @Test
    public void slackNotifierTest() {
        contest.setSlackNotifier(true);

        assertTrue(contest.getSlackNotifier());
    }

    @Test
    public void emailNotifierTest() {
        contest.setEmailNotifier(true);

        assertTrue(contest.getEmailNotifier());
    }

    @Test
    public void slackNotifierFalseTest() {
        contest.setSlackNotifier(false);

        assertFalse(contest.getSlackNotifier());
    }

    @Test
    public void emailNotifierFalseTest() {
        contest.setEmailNotifier(false);

        assertFalse(contest.getEmailNotifier());
    }
}
