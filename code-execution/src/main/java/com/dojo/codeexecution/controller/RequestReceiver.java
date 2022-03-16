package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.git.GitManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class RequestReceiver {

    @Autowired
    private GitManager gitManager;

    @GetMapping(value = "/repository")
    public String getRepository(@RequestParam(value = "username") String username,
                                @RequestParam(value = "game") String game,
                                @RequestParam(value = "templateURL") String templateURL) throws IOException {
        if (gitManager.hasUserExistingRepository(username, game)) {
            return gitManager.getExistingGitHubRepository(username, game).toString();
        } else {
            return gitManager.createGitHubRepository(username, game, templateURL).toString();
        }
    }

    @PostMapping(value = "/remove/collaborators")
    public String removeCollaboratorsForGame(@RequestParam(value = "game") String game){
        return gitManager.removeCollaboratorsForGame(game);
    }

    @DeleteMapping("/repository/{game}")
    public String deleteAllRepositoriesForAGame(@PathVariable("game") String game) throws IOException{
        return gitManager.deleteReposForParticularGame(game);
    }
}
