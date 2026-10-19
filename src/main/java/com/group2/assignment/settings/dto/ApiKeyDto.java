package com.group2.assignment.settings.dto;

import lombok.Getter;
import lombok.Setter;

/** Deliberately has no toString(): credentials must not appear in logs. */
@Getter
@Setter
public class ApiKeyDto {
    private String apiKey;
    private String provider = "GEMINI";
}
