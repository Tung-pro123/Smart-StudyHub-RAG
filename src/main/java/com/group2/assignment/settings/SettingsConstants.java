package com.group2.assignment.settings;

/**
 * Module 3: Settings & API Key Hub do Ân phụ trách.
 * - ApiKeyService.java (quản lý lưu API Key vào HttpSession)
 * - SettingsController.java (giao diện /settings kiểm tra ping kết nối Gemini)
 */
public final class SettingsConstants {
    public static final String SESSION_API_KEY = "GEMINI_API_KEY";
    public static final String SESSION_PROVIDER = "AI_PROVIDER";
    public static final String SESSION_PROVIDER_API_KEY = "AI_API_KEY";
    public static final String SESSION_CSRF_TOKEN = "SETTINGS_CSRF_TOKEN";
    public static final String DEFAULT_MODEL = "gemini-3.5-flash-lite";

    private SettingsConstants() {
    }
}
