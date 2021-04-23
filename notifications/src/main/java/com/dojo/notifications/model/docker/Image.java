package com.dojo.notifications.model.docker;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Image {
    private final String imageTag;
    private final String message;
}
