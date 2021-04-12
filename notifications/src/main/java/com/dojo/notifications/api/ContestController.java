package com.dojo.notifications.api;

import com.dojo.notifications.service.EventService;
import com.dojo.notifications.model.contest.Contest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ContestController {

    @Autowired
    private EventService eventService;

    @PostMapping("/api/v1/contest")
    public @ResponseBody
    ResponseEntity<Contest> subscribeForContest(@RequestBody Contest contest) {
        if (contest != null && contest.getContestId() != null) { // if exists
            stopNotifications(contest.getContestId());
        }
        eventService.addContest(contest);
        return new ResponseEntity<>(contest, HttpStatus.OK);
    }

    @PutMapping("/api/v1/contest")
    public @ResponseBody
    ResponseEntity<Contest> editContest(@RequestBody Contest contest) {
        return subscribeForContest(contest);
    }

    @DeleteMapping("/api/v1/contest/{id}")
    public @ResponseBody
    ResponseEntity<String> stopNotifications(@PathVariable String id) {
        Contest contest = eventService.getContestById(id);
        if (contest != null) {
            eventService.removeContest(contest.getContestId());
        }
        return new ResponseEntity<>("DELETE Response", HttpStatus.OK);
    }
}