package com.dojo.notifications.service;

import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.repo.SelectRequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SelectRequestService {
    private final SelectRequestRepo selectRequestRepo;

    @Autowired
    public SelectRequestService(SelectRequestRepo selectRequestRepo) {
        this.selectRequestRepo = selectRequestRepo;
    }

    public List<SelectRequest> getRequests() {
        List<SelectRequest> requests = new ArrayList<>();
        selectRequestRepo.findAll().forEach(requests::add);
        return Collections.unmodifiableList(requests);
    }

}
