package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.ActiveContestRequests;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.repo.ActiveRequestsRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ActiveRequestsService {

    private final ActiveRequestsRepo activeRequestsRepo;
    private final SelectRequestService selectRequestService;

    public ActiveRequestsService(ActiveRequestsRepo activeRequestsRepo, SelectRequestService selectRequestService) {
        this.activeRequestsRepo = activeRequestsRepo;
        this.selectRequestService = selectRequestService;
    }

    public List<ActiveContestRequests> getAllActiveRequests(){
        List<ActiveContestRequests> activeContestRequests = new ArrayList<>();

        activeRequestsRepo.findAll().forEach(activeContestRequests::add);

        return activeContestRequests;
    }

    public List<SelectRequest> getSelectRequestsForContest(String contestId){
        return getAllActiveRequests().stream()
                .filter(activeContestRequests -> activeContestRequests.getContestId().equals(contestId))
                .map(activeContestRequests -> selectRequestService.getRequestById(activeContestRequests.getQueryId()))
                .collect(Collectors.toList());
    }

    public void addActiveRequests(Set<Integer> queryIds, String contestId){
        removeCurrentRequestsForContest(contestId);

        if(queryIds!=null && contestId!=null){
            queryIds.forEach(queryId ->{
                ActiveContestRequests activeContestRequest = new ActiveContestRequests();
                activeContestRequest.setQueryId(queryId);
                activeContestRequest.setContestId(contestId);
                activeRequestsRepo.save(activeContestRequest);
            });
        }
    }

    public void removeCurrentRequestsForContest(String contestId){
        getAllActiveRequests().stream()
                .filter(activeContestRequests -> activeContestRequests.getContestId().equals(contestId))
                .forEach(activeRequestsRepo::delete);
    }
}
