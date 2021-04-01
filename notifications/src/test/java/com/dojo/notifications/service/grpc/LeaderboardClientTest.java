package com.dojo.notifications.service.grpc;

import com.dojo.apimock.ApiMockLeaderboardServiceGrpc;
import com.dojo.apimock.LeaderboardRequest;
import com.dojo.apimock.StartRequest;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.service.LeaderboardNotifierService;
import io.grpc.stub.StreamObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardClientTest {

    private static final String CONTEST_ID = "id";

    private Contest contest;

    private ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceBlockingStub blockingStub;
    private ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceStub stub;

    private LeaderboardClient leaderboardClient;

    @Before
    public void init() {
        contest = mock(Contest.class);
        LeaderboardNotifierService leaderboardNotifierService = mock(LeaderboardNotifierService.class);
        blockingStub = mock(ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceBlockingStub.class);
        stub = mock(ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceStub.class);

        this.leaderboardClient = new LeaderboardClient(leaderboardNotifierService, blockingStub, stub);

        when(contest.getContestId()).thenReturn(CONTEST_ID);
    }

    @Test
    public void startLeaderboardNotificationsTest() {
        StartRequest request = StartRequest.newBuilder().setContestId(CONTEST_ID).build();

        LeaderboardRequest leaderboardRequest = LeaderboardRequest.newBuilder().setContestId(CONTEST_ID).build();

        leaderboardClient.startLeaderboardNotifications(contest);

        verify(contest, times(2)).getContestId();
        verify(blockingStub, times(1)).startNotifications(request);
        verify(stub, times(1)).getLeaderboard(eq(leaderboardRequest), any(StreamObserver.class));
    }

    @Test
    public void stopLeaderboardNotificationsTest() {
        leaderboardClient.stopLeaderboardNotifications(CONTEST_ID);

        verify(blockingStub, times(1)).stopNotifications(any());
    }
}
