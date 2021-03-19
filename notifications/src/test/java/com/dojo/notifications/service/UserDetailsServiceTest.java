package com.dojo.notifications.service;

import com.dojo.notifications.configuration.Configuration;
import com.dojo.notifications.model.user.UserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserDetailsServiceTest {

    private static final long USER_ID = 1L;
    private static final String USER_EMAIL = "email@email";
    private static final String INVALID_URL = "http://localhost/invalid";

    private UserDetails testUser;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Configuration configuration;
    @Mock
    private RestTemplate restTemplate;


    private UserDetailsService userDetailsService;

    @Before
    public void init() {
        userDetailsService = new UserDetailsService(configuration);
        ReflectionTestUtils.setField(userDetailsService, "restTemplate", restTemplate);

        addUser();
    }

    @Test
    public void getUserDetailsTest() {
        UserDetails actual = userDetailsService.getUserDetails(USER_ID);

        assertEquals(testUser, actual);
    }

    @Test public void getUserEmailTest() {
        String actual = userDetailsService.getUserEmail(USER_ID);

        assertEquals(USER_EMAIL, actual);
    }

    private void addUser() {
        testUser = new UserDetails();
        testUser.setId(USER_ID);
        testUser.setEmail(USER_EMAIL);

        when(configuration.getUserDetailsApi()).thenReturn(INVALID_URL);
        String uriParameter = UriComponentsBuilder.fromHttpUrl(INVALID_URL).pathSegment(String.valueOf(USER_ID)).toUriString();
        when(restTemplate.exchange(uriParameter, HttpMethod.GET, null, new ParameterizedTypeReference<UserDetails>() {}))
                .thenReturn(new ResponseEntity<>(testUser, HttpStatus.ACCEPTED));
    }

}
