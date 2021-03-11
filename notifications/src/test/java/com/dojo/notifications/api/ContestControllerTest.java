package com.dojo.notifications.api;

import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.service.GamesService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContestControllerTest {

    @Mock
    private GamesService gamesService;

    @Mock
    private Contest contest;

    @InjectMocks
    private ContestController contestController;

    @BeforeEach
    public void init() {
        contestController = new ContestController();
    }

    @Test
    public void subscribeForContestTest() {
        //Arrange
        ResponseEntity<Contest> expected = new ResponseEntity<>(contest, HttpStatus.OK);
        //doNothing().when(gamesService).addContest(contest);

        //Act
        ResponseEntity<Contest> actual = contestController.subscribeForContest(contest);

        //Assert
        assertEquals(expected, actual);
        verify(gamesService, times(1)).addContest(contest);
    }

    @Test
    public void subscribeForContestWithStopTest() {
        //Arrange
        Contest notNull = new Contest();
        notNull.setContestId("1");
        ResponseEntity<Contest> expected = new ResponseEntity<>(notNull, HttpStatus.OK);
        //doNothing().when(gamesService).addContest(notNull);

        //Act
        ResponseEntity<Contest> actual = contestController.subscribeForContest(notNull);

        //Assert
        assertEquals(expected, actual);
        verify(gamesService, times(1)).addContest(notNull);
    }

    @Test
    public void editContestTest() {
        //Arrange
        ResponseEntity<Contest> expected = new ResponseEntity<>(contest, HttpStatus.OK);

        //Act
        ResponseEntity<Contest> actual = contestController.editContest(contest);

        //Assert
        assertEquals(expected, actual);
        verify(gamesService, times(1)).addContest(contest);
    }

    @Test
    public void stopNotificationsTest() {
        //Arrange
        ResponseEntity<String> expected = new ResponseEntity<>("DELETE Response", HttpStatus.OK);
        when(gamesService.getContestById("1")).thenReturn(contest);

        //Act
        ResponseEntity<String> actual = contestController.stopNotifications("1");

        //Assert
        assertEquals(expected, actual);
        verify(gamesService, times(1)).getContestById("1");
    }

    @Test
    public void stopNotificationsWithStopByIdTest() {
        //Arrange
        Contest notNull = new Contest();
        notNull.setContestId("1");
        ResponseEntity<String> expected = new ResponseEntity<>("DELETE Response", HttpStatus.OK);
        when(gamesService.getContestById("1")).thenReturn(contest);

        //Act
        ResponseEntity<String> actual = contestController.stopNotifications("1");

        //Assert
        assertEquals(expected, actual);
        verify(gamesService, times(1)).getContestById("1");
    }
}
