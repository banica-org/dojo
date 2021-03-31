package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.service.grpc.NotificationClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationManagingServiceTest {

    private static final int POOL_SIZE = 3;
    private static final int SCHEDULE_PERIOD = 5;
    private static final String CONTEST_ID = "id";

    private List<String> subscriptions;

    @Mock
    private NotificationClient notificationClient;

    private NotificationManagingService notificationManagingService;

    @Mock
    private Contest contest;

    @Before
    public void init() {
        notificationManagingService = new NotificationManagingService(notificationClient);
        ReflectionTestUtils.setField(notificationManagingService, "poolSize", POOL_SIZE);
        ReflectionTestUtils.setField(notificationManagingService, "schedulePeriod", SCHEDULE_PERIOD);

        when(contest.getContestId()).thenReturn(CONTEST_ID);

        subscriptions = (List<String>) ReflectionTestUtils.getField(notificationManagingService, "subscriptions");
    }

    @Test
    public void startNotificationsTest() {
        notificationManagingService.startNotifications(contest);

        assertEquals(1, subscriptions.size());
        assertTrue(subscriptions.contains(CONTEST_ID));
    }

    @Test
    public void stopNotificationsTest() {
        startNotificationsTest();
        notificationManagingService.stopNotifications(CONTEST_ID);

        assertTrue(subscriptions.isEmpty());
    }
}
