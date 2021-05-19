package com.dojo.notifications.grpc;

import com.codenjoy.dojo.LeaderboardRequest;
import com.codenjoy.dojo.LeaderboardServiceGrpc;
import com.codenjoy.dojo.StopRequest;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.service.notifierService.LeaderboardNotifierService;
import io.grpc.stub.StreamObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardClientTest {

    private static final String CONTEST_ID = "id";

    @Mock
    private Contest contest;

    @Mock
    private LeaderboardNotifierService leaderboardNotifierService;
    @Mock
    private LeaderboardServiceGrpc.LeaderboardServiceBlockingStub leaderboardServiceBlockingStub;
    @Mock
    private LeaderboardServiceGrpc.LeaderboardServiceStub leaderboardServiceStub;

    private LeaderboardClient leaderboardClient;

    @Before
    public void init() {
        this.leaderboardClient = new LeaderboardClient(leaderboardNotifierService, leaderboardServiceBlockingStub, leaderboardServiceStub);

        when(contest.getContestId()).thenReturn(CONTEST_ID);
    }

    @Test
    public void startLeaderboardNotificationsTest() {
        LeaderboardRequest leaderboardRequest = LeaderboardRequest.newBuilder().setContestId(CONTEST_ID).build();

        leaderboardClient.startLeaderboardNotifications(contest);

        verify(contest, times(2)).getContestId();
        verify(leaderboardServiceStub, times(1)).getLeaderboard(eq(leaderboardRequest), any(StreamObserver.class));
    }

    @Test
    public void stopLeaderboardNotificationsTest() {
        StopRequest request = StopRequest.newBuilder().setContestId(CONTEST_ID).build();

        leaderboardClient.stopLeaderboardNotifications(contest);

        verify(leaderboardServiceBlockingStub, times(1)).stopNotifications(request);
    }
}
