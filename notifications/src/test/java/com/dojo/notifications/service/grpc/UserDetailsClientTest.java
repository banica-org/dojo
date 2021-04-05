package com.dojo.notifications.service.grpc;

import com.dojo.apimock.ApiMockUserDetailsServiceGrpc;
import com.dojo.apimock.UserDetailsRequest;
import com.dojo.apimock.UserDetailsResponse;
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
    private ApiMockUserDetailsServiceGrpc.ApiMockUserDetailsServiceBlockingStub userDetailsServiceBlockingStub;

    private UserDetailsClient userDetailsClient;

    @Before
    public void init() {
        userDetailsClient = new UserDetailsClient(userDetailsServiceBlockingStub);
    }

    @Test
    public void getUserDetailsTest() {
        UserDetailsRequest request = UserDetailsRequest.newBuilder().setId(USER_ID).build();

        UserDetailsResponse response = mock(UserDetailsResponse.class);
        when(response.getId()).thenReturn(USER_ID);
        when(response.getEmail()).thenReturn(EMAIL);

        when(userDetailsServiceBlockingStub.getUserDetails(request)).thenReturn(response);

        userDetailsClient.getUserDetails(USER_ID);

        verify(userDetailsServiceBlockingStub, times(1)).getUserDetails(request);
        verify(response, times(1)).getId();
        verify(response, times(1)).getEmail();
    }

}
