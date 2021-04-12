package com.dojo.notifications.web;

import com.dojo.notifications.api.ContestController;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Game;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.request.SelectRequestModel;
import com.dojo.notifications.service.GamesService;
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

    @Autowired
    private SelectRequestService selectRequestService;

    @Autowired
    private GamesService gamesService;

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
                             @RequestParam(value = "action", required = true) String action) {
        if (action.equals("add")) {
            setupRequestPage(model, new SelectRequestModel());
            return "request";
        }
        Game selectedGame = gamesService.getGameById(newContest.getContestId());
        newContest.setTitle(selectedGame.getTitle());
        contestController.subscribeForContest(newContest);
        this.addDropDownOptions(model);
        return setupContestsPage(model, new Contest());
    }

    @GetMapping("/contest/open/{id}")
    public String editContest(@PathVariable String id, Model model) {
        Contest existingContest = gamesService.getContestById(id);
        this.addDropDownOptions(model);
        return setupContestsPage(model, existingContest);
    }

    @GetMapping("/contest/stop/{id}")
    public String stopContest(@PathVariable String id, Model model) {
        contestController.stopNotifications(id);
        this.addDropDownOptions(model);
        return setupContestsPage(model, new Contest());
    }

    @GetMapping("/games/refresh")
    public String gamesRefresh(Model model) {
        gamesService.invalidateGamesCache();
        return contestsPage(model);
    }

    @GetMapping("/request")
    public String requestsPage(Model model) {
        return setupRequestPage(model, new SelectRequestModel());
    }

    @PostMapping("/request")
    public String newRequest(@ModelAttribute SelectRequestModel newRequest, Model model,
                             @RequestParam(value = "action", required = true) String action) {
        if(action.equals("add")) {
            setupRequestPage(model, new SelectRequestModel());
            setupQueryUpdate(newRequest);
            addDropDownOptions(model);
            return redirect(model);
        }
        return redirect(model);
    }

    private String setupContestsPage(Model model, Contest contest) {
        model.addAttribute("newContest", contest);
        model.addAttribute("games", gamesService.getAllGames());
        model.addAttribute("contests", gamesService.getAllContests());

        return "contest";
    }

    private String setupRequestPage(Model model, SelectRequestModel newRequest) {
        model.addAttribute("newRequest", newRequest);
        model.addAttribute("queryParameters", newRequest.getQueryParameters());
        model.addAttribute("querySpecification", newRequest.getQuerySpecification());
        model.addAttribute("describingMessage", newRequest.getDescribingMessage());
        model.addAttribute("eventType", newRequest.getEventType());
        model.addAttribute("communicationLevel", newRequest.getCommunicationLevel());

        return "request";
    }

    private void setupQueryUpdate(SelectRequestModel newRequest) {
        SelectRequest selectRequest = new SelectRequest();

        selectRequest.setQuery("SELECT " + newRequest.getQueryParameters() + " FROM %s " + newRequest.getQuerySpecification());
        selectRequest.setMessage(newRequest.getDescribingMessage());
        selectRequest.setEventType(newRequest.getEventType());
        selectRequest.setCommunicationLevel(newRequest.getCommunicationLevel(newRequest.getEventType()));
        selectRequestService.saveRequest(selectRequest);
    }

    public void addDropDownOptions(Model model) {
        List<SelectRequest> selectRequestList = selectRequestService.getRequests();
        model.addAttribute("queries", selectRequestList);
    }
}
