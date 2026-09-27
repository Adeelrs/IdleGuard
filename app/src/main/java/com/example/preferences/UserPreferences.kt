package com.example.preferences

enum class AppColorTheme(val id: String, val displayName: String, val primaryHex: Long) {
    EMERALD("EMERALD", "Emerald Focus", 0xFF10B981),
    SKY("SKY", "Sky Minimal", 0xFF0284C7),
    VIOLET("VIOLET", "Deep Violet", 0xFF8B5CF6),
    AMBER("AMBER", "Warm Amber", 0xFFF59E0B),
    ROSE("ROSE", "Crimson Rose", 0xFFF43F5E),
    MONOCHROME("MONOCHROME", "Slate Titanium", 0xFF64748B);

    companion object {
        fun fromId(id: String): AppColorTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: EMERALD
        }
    }
}

data class UserPreferences(
    val repeatIntervalMinutes: Int = 15,
    val colorTheme: String = AppColorTheme.EMERALD.id,
    val isNudgeEnabled: Boolean = true,
    val defaultSnoozeMinutes: Int = 5,
    val dailyScreenTimeGoalMinutes: Int = 120,
    val dailyFocusGoalMinutes: Int = 60,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val idleAutoDetectMinutes: Int = 3,
    val persistentServiceEnabled: Boolean = true
)
