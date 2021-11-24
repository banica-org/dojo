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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SelectRequestService {
    private static final String JOIN = "join";

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
            if (request.getQuery().contains(tableName) && !request.getQuery().contains(JOIN)) {
                requests.add(request);
            }
        });
        return requests;
    }

    public SelectRequest getRequestById(int id) {
        Optional<SelectRequest> request = selectRequestRepo.findById(id);

        return request.orElse(null);
    }

    public List<SelectRequest> getJoinSelectRequestsForTable(String tableName) {
        List<SelectRequest> requests = new ArrayList<>();
        selectRequestRepo.findAll().forEach(request -> {
            if (request.getQuery().contains(tableName) && request.getQuery().contains(JOIN)) {
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

    public Set<SelectRequest> getSpecificRequests(Set<Integer> ids, List<SelectRequest> selectRequests) {
        return selectRequests.stream()
                .filter(request -> ids.contains(request.getId()))
                .collect(Collectors.toSet());
    }
}
