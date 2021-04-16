package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.grpc.leaderboard.LeaderboardClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationManagingServiceTest {

    private static final String CONTEST_ID = "id";

    @Mock
    private LeaderboardClient leaderboardClient;

    private NotificationManagingService notificationManagingService;

    @Mock
    private Contest contest;

    @Before
    public void init() {
        notificationManagingService = new NotificationManagingService(leaderboardClient);
        when(contest.getContestId()).thenReturn(CONTEST_ID);
    }

    @Test
    public void startNotificationsTest() {
        notificationManagingService.startNotifications(contest);
        verify(leaderboardClient, times(1)).startLeaderboardNotifications(contest);
    }

    @Test
    public void stopNotificationsTest() {
        startNotificationsTest();
        notificationManagingService.stopNotifications(CONTEST_ID);
        verify(leaderboardClient, times(1)).stopLeaderboardNotifications(CONTEST_ID);
    }
}
