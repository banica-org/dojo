package com.dojo.notifications.api;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.service.ActiveRequestsService;
import com.dojo.notifications.service.EventService;
import com.dojo.notifications.service.NotificationManagingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ContestControllerTest {

    private static final String CONTEST_ID = "1";

    @Mock
    private EventService eventService;

    @Mock
    private NotificationManagingService notificationManagingService;

    @Mock
    private ActiveRequestsService activeRequestsService;

    @Mock
    private Contest contest;

    @InjectMocks
    private ContestController contestController;


    @Test
    public void subscribeForMockContestTest() {
        //Arrange
        ResponseEntity<Contest> expected = new ResponseEntity<>(contest, HttpStatus.OK);

        //Act
        ResponseEntity<Contest> actual = contestController.subscribeForContest(contest);

        //Assert
        assertEquals(expected, actual);
        verify(eventService, times(1)).addContest(contest);
        verify(notificationManagingService, times(1)).startNotifications(contest);
    }

    @Test
    public void subscribeForNotNullContestTest() {
        //Arrange
        Contest notNull = new Contest();
        notNull.setContestId(CONTEST_ID);
        ResponseEntity<Contest> expected = new ResponseEntity<>(notNull, HttpStatus.OK);

        //Act
        ResponseEntity<Contest> actual = contestController.subscribeForContest(notNull);

        //Assert
        assertEquals(expected, actual);
        verify(eventService, times(1)).addContest(notNull);
        verify(eventService, times(1)).getContestById(CONTEST_ID);
        verify(notificationManagingService, times(1)).startNotifications(notNull);
    }

    @Test
    public void editForMockContestTest() {
        //Arrange
        ResponseEntity<Contest> expected = new ResponseEntity<>(contest, HttpStatus.OK);

        //Act
        ResponseEntity<Contest> actual = contestController.editContest(contest);

        //Assert
        assertEquals(expected, actual);
        verify(eventService, times(1)).addContest(contest);
    }

    @Test
    public void editForNotNullContestTest() {
        //Arrange
        Contest notNull = new Contest();
        notNull.setContestId("1");
        ResponseEntity<Contest> expected = new ResponseEntity<>(notNull, HttpStatus.OK);

        //Act
        ResponseEntity<Contest> actual = contestController.editContest(notNull);

        //Assert
        assertEquals(expected, actual);
        verify(eventService, times(1)).addContest(notNull);
        verify(eventService, times(1)).getContestById(CONTEST_ID);

    }

    @Test
    public void stopNotificationsForNullContestTest() {
        //Arrange
        ResponseEntity<String> expected = new ResponseEntity<>("DELETE Response", HttpStatus.OK);
        when(eventService.getContestById(CONTEST_ID)).thenReturn(null);

        //Act
        ResponseEntity<String> actual = contestController.stopNotifications(CONTEST_ID);

        //Assert
        assertEquals(expected, actual);
        verify(eventService, times(1)).getContestById(CONTEST_ID);
    }

    @Test
    public void stopNotificationsForMockContestTest() {
        //Arrange
        ResponseEntity<String> expected = new ResponseEntity<>("DELETE Response", HttpStatus.OK);
        when(eventService.getContestById(CONTEST_ID)).thenReturn(contest);

        //Act
        ResponseEntity<String> actual = contestController.stopNotifications(CONTEST_ID);

        //Assert
        assertEquals(expected, actual);
        verify(eventService, times(1)).getContestById(CONTEST_ID);
        verify(eventService, times(1)).removeContest(CONTEST_ID);
        verify(notificationManagingService, times(1)).stopNotifications(contest);
    }
}
