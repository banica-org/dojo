package com.dojo.codeexecution.model;

import java.util.List;

public class TestResult {
    private String username;
    private int points;
    private List<FailedTestCase> failedTestCases;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public List<FailedTestCase> getFailedTestCases() {
        return failedTestCases;
    }

    public void setFailedTestCases(List<FailedTestCase> failedTestCases) {
        this.failedTestCases = failedTestCases;
    }
}
