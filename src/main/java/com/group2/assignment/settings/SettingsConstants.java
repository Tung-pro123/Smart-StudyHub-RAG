package com.group2.assignment.settings;

/**
 * Module 3: Settings & API Key Hub do Ân phụ trách.
 * - ApiKeyService.java (quản lý lưu API Key vào HttpSession)
 * - SettingsController.java (giao diện /settings kiểm tra ping kết nối Gemini)
 */
public class SettingsConstants {
    public static final String SESSION_API_KEY = "GEMINI_API_KEY";
    public static final String DEFAULT_MODEL = "gemini-1.5-flash";
}
