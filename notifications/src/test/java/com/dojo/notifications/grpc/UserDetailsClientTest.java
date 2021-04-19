package com.dojo.notifications.grpc;

import com.codenjoy.dojo.ByIdRequest;
import com.codenjoy.dojo.UserDetailsResponse;
import com.codenjoy.dojo.UserDetailsServiceGrpc;
import com.dojo.notifications.grpc.leaderboard.UserDetailsClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserDetailsClientTest {

    private static final String USER_ID = "id";
    private static final String EMAIL = "email";

    @Mock
    private UserDetailsServiceGrpc.UserDetailsServiceBlockingStub userDetailsServiceBlockingStub;

    private UserDetailsClient userDetailsClient;

    @Before
    public void init() {
        userDetailsClient = new UserDetailsClient(userDetailsServiceBlockingStub);
    }

    @Test
    public void getUserDetailsTest() {
        ByIdRequest request = ByIdRequest.newBuilder().setId(USER_ID).build();

        UserDetailsResponse response = mock(UserDetailsResponse.class);
        when(response.getId()).thenReturn(USER_ID);
        when(response.getEmail()).thenReturn(EMAIL);

        when(userDetailsServiceBlockingStub.getUserDetailsById(request)).thenReturn(response);

        userDetailsClient.getUserDetailsById(USER_ID);

        verify(userDetailsServiceBlockingStub, times(1)).getUserDetailsById(request);
        verify(response, times(1)).getId();
        verify(response, times(1)).getEmail();
    }

}
