package com.dojo.notifications.web;

import com.dojo.notifications.api.ContestController;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Event;
import com.dojo.notifications.model.user.UserManagement;
import com.dojo.notifications.service.EventService;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.request.SelectRequestModel;
import com.dojo.notifications.service.EventService;
import com.dojo.notifications.service.FlinkTableService;
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
    private final static String ACTION_START = "start";
    private final static String ACTION_BACK = "back";

    @Autowired
    private SelectRequestService selectRequestService;

    @Autowired
    private FlinkTableService flinkTableService;

    @Autowired
    private EventService eventService;

    @Autowired
    private ContestController contestController;

    @Autowired
    private UserManagement userManagement;

    @GetMapping("/")
    public String redirect(Model model) {
        return contestsPage(model);
    }

    @GetMapping("/contest")
    public String contestsPage(Model model) {
        return setupContestsPage(model, new Contest());
    }

    @PostMapping("/event")
    public String determineEvent(@ModelAttribute Contest newContest, Model model, @RequestParam String action) {
        switch (action) {
            case ACTION_ADD:
                return setupRequestPage(model, new SelectRequestModel(), newContest);
            case ACTION_START:
                return newContest(newContest, model);
            case ACTION_BACK:
                return redirect(model);
            default:
                return deleteRequest(Integer.parseInt(action), model);
        }
    }

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

    @GetMapping("/request")
    public String requestsPage(Model model, Contest contest) {
        return setupRequestPage(model, new SelectRequestModel(), contest);
    }

    @PostMapping("/request")
    public String newRequest(@ModelAttribute SelectRequestModel newRequest, Model model, @RequestParam String action, Contest contest) {
        if (action.equals(ACTION_ADD)) {
            setupQueryUpdate(newRequest);
        }
        return redirect(model);

    }

    public String deleteRequest(@PathVariable int id, Model model) {
        selectRequestService.deleteRequest(id);
        return contestsPage(model);
    }

    private String setupContestsPage(Model model, Contest contest) {
        List<SelectRequest> selectRequestList = selectRequestService.getAllRequests();

        model.addAttribute("newContest", contest);
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("contests", eventService.getAllContests());
        model.addAttribute("queries", selectRequestList);
        model.addAttribute("queryIds", contest.getQueryIds());

        return "contest";
    }

    private String setupRequestPage(Model model, SelectRequestModel newRequest, Contest newContest) {
        model.addAttribute("newRequest", newRequest);
        model.addAttribute("queryParameters", newRequest.getQueryParameters());
        model.addAttribute("queryTable", newRequest.getQueryTable());
        model.addAttribute("querySpecification", newRequest.getQuerySpecification());
        model.addAttribute("describingMessage", newRequest.getNotificationMessage());
        model.addAttribute("notificationMessage", newRequest.getDescribingMessage());
        model.addAttribute("receivers", newRequest.getReceivers());
        model.addAttribute("users", userManagement.getAllAutocomplete(newContest.getContestId()));

        model.addAttribute("tables", flinkTableService.getTables());

        return "request";
    }

    private void setupQueryUpdate(SelectRequestModel newRequest) {
        SelectRequest selectRequest = new SelectRequest();

        selectRequest.setQuery("SELECT " + newRequest.getQueryParameters() + " FROM " + newRequest.getQueryTable().toLowerCase() + " " + newRequest.getQuerySpecification());
        selectRequest.setQueryDescription(newRequest.getDescribingMessage());
        selectRequest.setMessage(newRequest.getNotificationMessage());
        selectRequest.setReceivers(newRequest.getReceivers());

        selectRequestService.saveRequest(selectRequest);
    }

}