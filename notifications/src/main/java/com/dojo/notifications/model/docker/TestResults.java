package com.dojo.notifications.model.docker;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class TestResults {
    private final String username;
    private final Map<String, String> failedTestCases;
}
