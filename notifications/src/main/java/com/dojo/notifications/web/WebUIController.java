package com.dojo.notifications.web;

import com.dojo.notifications.api.ContestController;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Game;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.request.SelectRequestModel;
import com.dojo.notifications.service.GamesService;
import com.dojo.notifications.service.SelectRequestService;
import jdk.nashorn.internal.objects.annotations.Getter;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
        return setupContestsPage(model, new Contest(), new SelectRequestModel());
    }

    @PostMapping("/contest")
    public String newContest(@ModelAttribute Contest newContest, @ModelAttribute SelectRequestModel newRequest, Model model) {
        Game selectedGame = gamesService.getGameById(newContest.getContestId());
        newContest.setTitle(selectedGame.getTitle());
        contestController.subscribeForContest(newContest);
        return setupContestsPage(model, new Contest(), new SelectRequestModel());
    }

    @GetMapping("/request")
    public String requestsPage(Model model) {
        return setupRequestPage(model, new SelectRequestModel());
    }

    @PostMapping("/request")
    public String newRequest(@ModelAttribute SelectRequestModel newRequest, Model model) {
        setupRequestPage(model, new SelectRequestModel());
        setupQueryUpdate(newRequest);
        return redirect(model);
    }

    @GetMapping("/contest/open/{id}")
    public String editContest(@PathVariable String id, Model model) {
        Contest existingContest = gamesService.getContestById(id);
        return setupContestsPage(model, existingContest, new SelectRequestModel());
    }

    @GetMapping("/contest/stop/{id}")
    public String stopContest(@PathVariable String id, Model model) {
        contestController.stopNotifications(id);
        return setupContestsPage(model, new Contest(), new SelectRequestModel());
    }

    @GetMapping("/games/refresh")
    public String gamesRefresh(Model model) {
        gamesService.invalidateGamesCache();
        return contestsPage(model);
    }

    private String setupContestsPage(Model model, Contest contest, SelectRequestModel newRequest) {
        model.addAttribute("newContest", contest);
        model.addAttribute("games", gamesService.getAllGames());
        model.addAttribute("contests", gamesService.getAllContests());

        if (newRequest != null) {
            setupRequestPage(model, newRequest);
        }
        return "contest";
    }

    private String setupRequestPage(Model model, SelectRequestModel newRequest) {
        model.addAttribute("newRequest", newRequest);
        model.addAttribute("queryParameters", newRequest.getQueryParameters());
        model.addAttribute("querySpecification", newRequest.getQuerySpecification());
        model.addAttribute("describingMessage", newRequest.getDescribingMessage());

        return "request";
    }

    private void setupQueryUpdate(SelectRequestModel newRequest) {
        SelectRequest selectRequest = new SelectRequest();

        selectRequest.setQuery("SELECT " + newRequest.getQueryParameters() + " FROM %s " + newRequest.getQuerySpecification());
        selectRequest.setMessage(newRequest.getDescribingMessage());
        selectRequest.setEventType("POSITION_CHANGES");
        selectRequest.setQuantifier("ALL");
        selectRequestService.saveRequest(selectRequest);
    }
}
