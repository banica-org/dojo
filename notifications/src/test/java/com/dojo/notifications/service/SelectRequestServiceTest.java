package com.dojo.notifications.service;

import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.repo.SelectRequestRepo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class SelectRequestServiceTest {

    @Mock
    private SelectRequestRepo selectRequestRepo;

    private SelectRequestService selectRequestService;

    @Before
    public void init() {
        selectRequestService = new SelectRequestService(selectRequestRepo);
    }

    @Test
    public void getRequestsTest() {
        //Arrange
        List<SelectRequest> expected = new ArrayList<>();
        when(selectRequestRepo.findAll()).thenReturn(expected);

        //Act
        List<SelectRequest> actual = selectRequestService.getAllRequests();

        //Assert
        Assert.assertEquals(expected, actual);

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

}
