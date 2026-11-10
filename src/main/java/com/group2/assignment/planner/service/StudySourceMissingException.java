package com.group2.assignment.planner.service;

public class StudySourceMissingException extends RuntimeException {
    public StudySourceMissingException() {
        super("Study source is missing.");
    }
}
