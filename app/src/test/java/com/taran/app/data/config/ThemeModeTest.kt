package com.taran.app.data.config

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {
    @Test
    fun knownChoicesRemainUnchanged() {
        for (mode in ThemeMode.ALL) assertEquals(mode, ThemeMode.normalize(mode))
    }

    @Test
    fun absentOrUnknownChoiceFallsBackToSystem() {
        for (mode in listOf(null, "", "unknown", "DARK")) {
            assertEquals(ThemeMode.SYSTEM, ThemeMode.normalize(mode))
        }
    }
}
