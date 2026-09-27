package com.example

import com.example.preferences.AppColorTheme
import com.example.preferences.UserPreferences
import com.example.ui.screens.WeekDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyTaskAndPreferencesTest {

    @Test
    fun testWeekDayEnumIntegrity() {
        assertEquals(7, WeekDay.entries.size)
        assertEquals("Monday", WeekDay.MONDAY.fullName)
        assertEquals("Sunday", WeekDay.SUNDAY.fullName)
        assertEquals("Fri", WeekDay.FRIDAY.shortName)
    }

    @Test
    fun testAppColorThemeFromId() {
        assertEquals(AppColorTheme.EMERALD, AppColorTheme.fromId("EMERALD"))
        assertEquals(AppColorTheme.SKY, AppColorTheme.fromId("sky"))
        assertEquals(AppColorTheme.VIOLET, AppColorTheme.fromId("VIOLET"))
        assertEquals(AppColorTheme.AMBER, AppColorTheme.fromId("AMBER"))
        assertEquals(AppColorTheme.ROSE, AppColorTheme.fromId("ROSE"))
        assertEquals(AppColorTheme.MONOCHROME, AppColorTheme.fromId("MONOCHROME"))
        assertEquals(AppColorTheme.EMERALD, AppColorTheme.fromId("unknown_fallback"))
    }

    @Test
    fun testDefaultUserPreferences() {
        val prefs = UserPreferences()
        assertEquals(15, prefs.repeatIntervalMinutes)
        assertEquals(AppColorTheme.EMERALD.id, prefs.colorTheme)
        assertTrue(prefs.isNudgeEnabled)
    }
}
