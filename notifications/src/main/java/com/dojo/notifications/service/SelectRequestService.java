package com.dojo.notifications.service;

import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.repo.SelectRequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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

    public List<SelectRequest> getAllRequests() {
        List<SelectRequest> requests = new ArrayList<>();
        selectRequestRepo.findAll().forEach(requests::add);
        return Collections.unmodifiableList(requests);
    }

    public List<SelectRequest> getRequestsForTable(String tableName) {
        List<SelectRequest> requests = new ArrayList<>();
        selectRequestRepo.findAll().forEach(request -> {
            if (request.getQuery().contains(tableName)) {
                requests.add(request);
            }
        });
        return requests;
    }

    public void saveRequest(@RequestBody SelectRequest selectRequest) {
        selectRequestRepo.save(selectRequest);
    }

    public void deleteRequest(@PathVariable int id) {
        selectRequestRepo.deleteById(id);
    }
}
