package com.dojo.notifications.model.docker;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Container {
    private final String username;
    private final String status;
    private final String codeExecution;
    private final List<String> logs;
}
