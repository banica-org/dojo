package com.dojo.notifications.web;

import com.dojo.notifications.api.ContestController;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Event;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.request.SelectRequestModel;
import com.dojo.notifications.service.EventService;
import com.dojo.notifications.service.SelectRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebUIController {

    private final static String ACTION_ADD = "add";

    @Autowired
    private SelectRequestService selectRequestService;

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
        this.addDropDownOptions(model);
        return setupContestsPage(model, new Contest());
    }

    @PostMapping("/contest")
    public String newContest(@ModelAttribute Contest newContest, Model model,
                             @RequestParam String action) {
        if (action.equals(ACTION_ADD)) {
            setupRequestPage(model, new SelectRequestModel());
            return "request";
        }
        Event selectedEvent = eventService.getEventByRoomName(newContest.getContestId());
        newContest.setTitle(selectedEvent.getGameName());
        contestController.subscribeForContest(newContest);
        this.addDropDownOptions(model);
        return setupContestsPage(model, new Contest());
    }

    @GetMapping("/contest/open/{id}")
    public String editContest(@PathVariable String id, Model model) {
        Contest existingContest = eventService.getContestById(id);
        this.addDropDownOptions(model);
        return setupContestsPage(model, existingContest);
    }

    @GetMapping("/contest/stop/{id}")
    public String stopContest(@PathVariable String id, Model model) {
        contestController.stopNotifications(id);
        this.addDropDownOptions(model);
        return setupContestsPage(model, new Contest());
    }

    @GetMapping("/events/refresh")
    public String eventsRefresh(Model model) {
        eventService.invalidateEventsCache();
        return contestsPage(model);
    }

    @GetMapping("/request")
    public String requestsPage(Model model) {
        return setupRequestPage(model, new SelectRequestModel());
    }

    @PostMapping("/request")
    public String newRequest(@ModelAttribute SelectRequestModel newRequest, Model model,
                             @RequestParam String action) {
        if (action.equals(ACTION_ADD)) {
            setupRequestPage(model, new SelectRequestModel());
            setupQueryUpdate(newRequest);
            addDropDownOptions(model);
            return redirect(model);
        }
        return redirect(model);
    }

    private String setupContestsPage(Model model, Contest contest) {
        model.addAttribute("newContest", contest);
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("contests", eventService.getAllContests());

        return "contest";
    }

    private String setupRequestPage(Model model, SelectRequestModel newRequest) {
        model.addAttribute("newRequest", newRequest);
        model.addAttribute("queryParameters", newRequest.getQueryParameters());
        model.addAttribute("queryTable", newRequest.getQueryTable());
        model.addAttribute("querySpecification", newRequest.getQuerySpecification());
        model.addAttribute("notify", newRequest.getReceiver());
        model.addAttribute("describingMessage", newRequest.getNotificationMessage());
        model.addAttribute("notificationMessage", newRequest.getDescribingMessage());

        return "request";
    }

    private void setupQueryUpdate(SelectRequestModel newRequest) {
        SelectRequest selectRequest = new SelectRequest();

        selectRequest.setQuery("SELECT " + newRequest.getQueryParameters() + " FROM " + newRequest.getQueryTable().toLowerCase() + " " + newRequest.getQuerySpecification());
        selectRequest.setReceiver(newRequest.getReceiver());
        selectRequest.setQueryDescription(newRequest.getDescribingMessage());
        selectRequest.setMessage(newRequest.getNotificationMessage());

        selectRequestService.saveRequest(selectRequest);
    }

    public void addDropDownOptions(Model model) {
        List<SelectRequest> selectRequestList = selectRequestService.getAllRequests();
        model.addAttribute("queries", selectRequestList);
    }
}
