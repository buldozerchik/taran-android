package com.taran.app.data.backup

import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsBackupCaptchaTest {
    private fun fixture() = BackupData(
        servers = emptyList(),
        activeId = null,
        ownClientId = "0123456789abcdef0123456789abcdef",
        dynamicTheme = true,
        nerdMode = true,
        privacyMode = false,
        seasonalDecor = true,
        restartServerOnSwitch = false,
        autoConnect = false,
        hotspotProxy = false,
        suppressUpdatePrompt = false,
        suppressTgPrompt = false,
        advancedCaptcha = true,
    )

    @Test
    fun preservesAdvancedCaptchaChoice() {
        val restored = SettingsBackup.decode(SettingsBackup.encode(fixture()))
        assertTrue(restored.advancedCaptcha)
    }

    @Test
    fun oldBackupDefaultsToLegacyAutomaticCaptcha() {
        val old = JSONObject(SettingsBackup.encode(fixture())).apply {
            remove("advancedCaptcha")
        }.toString()
        assertFalse(SettingsBackup.decode(old).advancedCaptcha)
    }

    @Test
    fun preservesSharedSplitRulesAndProfileSelection() {
        val profile = com.taran.app.data.server.Server(
            name = "shared",
            client = com.taran.app.data.config.ClientConfig(splitTunnelShared = true)
        )
        val data = fixture().copy(
            servers = listOf(profile),
            activeId = profile.id,
            splitSharedMode = "include",
            splitSharedApps = "org.example.one org.example.two"
        )
        val restored = SettingsBackup.decode(SettingsBackup.encode(data))
        assertEquals(data.splitSharedMode, restored.splitSharedMode)
        assertEquals(data.splitSharedApps, restored.splitSharedApps)
        assertEquals(profile.id, restored.activeId)
        assertTrue(restored.servers.single().client.splitTunnelShared)
    }

    @Test
    fun oldBackupHasNoSharedSplitRules() {
        val data = fixture().copy(splitSharedMode = "include", splitSharedApps = "org.example.one")
        val old = JSONObject(SettingsBackup.encode(data)).apply {
            remove("splitSharedMode")
            remove("splitSharedApps")
        }.toString()
        val restored = SettingsBackup.decode(old)
        assertNull(restored.splitSharedMode)
        assertEquals("", restored.splitSharedApps)
    }

    @Test
    fun themeAndAmoledSurviveBackup() {
        val data = fixture().copy(themeMode = "dark", amoled = true)
        val restored = SettingsBackup.decode(SettingsBackup.encode(data))
        assertEquals("dark", restored.themeMode)
        assertTrue(restored.amoled)
    }

    @Test
    fun oldBackupUsesSystemThemeWithoutAmoled() {
        val old = JSONObject(SettingsBackup.encode(fixture())).apply {
            remove("themeMode")
            remove("amoled")
        }.toString()
        val restored = SettingsBackup.decode(old)
        assertEquals("system", restored.themeMode)
        assertFalse(restored.amoled)
    }
}
