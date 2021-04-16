package com.dojo.notifications.web;

import com.dojo.notifications.api.ContestController;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Event;
import com.dojo.notifications.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebUIController {

    @Autowired
    private EventService eventService;

    @Autowired
    private ContestController contestController;

    @GetMapping("/")
    public String redirect(Model model) {
        return contestsPage(model);
    }

    @GetMapping("/contest")
    public String contestsPage(Model model) {
        return setupContestsPage(model, new Contest());
    }

    @PostMapping("/contest")
    public String newContest(@ModelAttribute Contest newContest, Model model) {
        Event selectedEvent = eventService.getEventByRoomName(newContest.getContestId());
        newContest.setTitle(selectedEvent.getGameName());
        contestController.subscribeForContest(newContest);
        return setupContestsPage(model, new Contest());
    }

    @GetMapping("/contest/open/{id}")
    public String editContest(@PathVariable String id, Model model) {
        Contest existingContest = eventService.getContestById(id);
        return setupContestsPage(model, existingContest);
    }

    @GetMapping("/contest/stop/{id}")
    public String stopContest(@PathVariable String id, Model model) {
        contestController.stopNotifications(id);
        return setupContestsPage(model, new Contest());
    }

    @GetMapping("/events/refresh")
    public String eventsRefresh(Model model) {
        eventService.invalidateEventsCache();
        return contestsPage(model);
    }

    private String setupContestsPage(Model model, Contest contest) {
        model.addAttribute("newContest", contest);
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("contests", eventService.getAllContests());
        return "contest";
    }
}
