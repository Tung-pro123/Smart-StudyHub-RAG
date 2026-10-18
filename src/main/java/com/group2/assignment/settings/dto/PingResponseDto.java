package com.group2.assignment.settings.dto;

public record PingResponseDto(boolean success, long latencyMs, String message) {
}
