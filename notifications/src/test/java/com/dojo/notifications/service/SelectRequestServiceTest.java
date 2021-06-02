package com.dojo.notifications.service;

import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.repo.SelectRequestRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class SelectRequestServiceTest {

    private static final int REQUEST_ID = 1;

    private static final String TABLE_NAME = "leaderboard";
    private static final String LEADERBOARD_QUERY = "SELECT * FROM leaderboard";
    private static final String DOCKER_QUERY = "SELECT * FROM docker_events";
    private static final String JOIN_QUERY = "SELECT * FROM leaderboard join docker_events";

    private final SelectRequest leaderboardRequest = new SelectRequest();
    private final SelectRequest dockerRequest = new SelectRequest();
    private final SelectRequest joinQuery = new SelectRequest();
    private final List<SelectRequest> allRequests = new ArrayList<>();


    @Mock
    private SelectRequestRepo selectRequestRepo;

    private SelectRequestService selectRequestService;

    @Before
    public void init() {
        selectRequestService = new SelectRequestService(selectRequestRepo);
        leaderboardRequest.setQuery(LEADERBOARD_QUERY);
        dockerRequest.setQuery(DOCKER_QUERY);
        joinQuery.setQuery(JOIN_QUERY);

        allRequests.add(leaderboardRequest);
        allRequests.add(dockerRequest);
        allRequests.add(joinQuery);

        when(selectRequestRepo.findAll()).thenReturn(allRequests);
    }

    @Test
    public void getAllRequestsTest() {
        //Arrange

        //Act
        List<SelectRequest> actual = selectRequestService.getAllRequests();

        //Assert
        assertEquals(allRequests, actual);
        verify(selectRequestRepo, times(1)).findAll();
    }

    @Test
    public void getRequestsForTableTest() {
        List<SelectRequest> expected = Collections.singletonList(leaderboardRequest);

        List<SelectRequest> actual = selectRequestService.getRequestsForTable(TABLE_NAME);

        assertEquals(expected, actual);
        verify(selectRequestRepo, times(1)).findAll();
    }

    @Test
    public void saveRequestTest() {
        //Arrange
        SelectRequest expected = new SelectRequest();
        when(selectRequestRepo.save(expected)).thenReturn(expected);

        //Act
        selectRequestService.saveRequest(expected);

        //Assert
        verify(selectRequestRepo, times(1)).save(expected);
    }

    @Test
    public void deleteRequestTest() {
        dockerRequest.setId(REQUEST_ID);
        selectRequestService.deleteRequest(REQUEST_ID);
        verify(selectRequestRepo, times(1)).deleteById(REQUEST_ID);
    }

    @Test
    public void getSpecificRequestsTest() {
        leaderboardRequest.setId(REQUEST_ID);
        Set<SelectRequest> expected = Collections.singleton(leaderboardRequest);

        Set<SelectRequest> actual = selectRequestService.getSpecificRequests(Collections.singleton(REQUEST_ID), allRequests);

        assertEquals(expected, actual);
    }

    @Test
    public void getJoinSelectRequestsForTableTest() {
        List<SelectRequest> expected = Collections.singletonList(joinQuery);

        List<SelectRequest> actual = selectRequestService.getJoinSelectRequestsForTable(TABLE_NAME);

        assertEquals(expected, actual);
        verify(selectRequestRepo, times(1)).findAll();
    }
}
