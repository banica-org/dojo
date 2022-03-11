package com.dojo.codeexecution.service.git;

import java.util.Collections;
import java.util.List;

public class ResultGenerator {

    public static String generateResult(List<String> deletedRepos) {
        if(deletedRepos.size() == 0) {
            return "No repositories for that game have been found";
        }
        else {
            Collections.sort(deletedRepos);
            return "Following repositories have been deleted:" + System.lineSeparator()
                    + String.join(System.lineSeparator(), deletedRepos);
        }
    }
}
