package com.dojo.notifications.service.grpc;

import com.dojo.apimock.ApiMockUserDetailsServiceGrpc;
import com.dojo.apimock.UserDetailsRequest;
import com.dojo.apimock.UserDetailsResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserDetailsClientTest {

    private static final String USER_ID = "id";
    private static final String EMAIL = "email";

    private UserDetailsClient userDetailsClient;
    private ApiMockUserDetailsServiceGrpc.ApiMockUserDetailsServiceBlockingStub blockingStub;

    @Before
    public void init() {
        blockingStub = mock(ApiMockUserDetailsServiceGrpc.ApiMockUserDetailsServiceBlockingStub.class);
        userDetailsClient = new UserDetailsClient(blockingStub);
    }

    @Test
    public void getUserDetailsTest() {
        UserDetailsRequest request = UserDetailsRequest.newBuilder().setId(USER_ID).build();

        UserDetailsResponse response = mock(UserDetailsResponse.class);
        when(response.getId()).thenReturn(USER_ID);
        when(response.getEmail()).thenReturn(EMAIL);

        when(blockingStub.getUserDetails(request)).thenReturn(response);

        userDetailsClient.getUserDetails(USER_ID);

        verify(blockingStub, times(1)).getUserDetails(request);
        verify(response, times(1)).getId();
        verify(response, times(1)).getEmail();
    }

}
