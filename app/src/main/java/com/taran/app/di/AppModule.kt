package com.taran.app.di

import com.taran.app.data.AppPreferences
import com.taran.app.domain.backup.BackupManager
import com.taran.app.domain.update.AppUpdater
import com.taran.app.domain.share.LinkImportBus
import com.taran.app.domain.proxy.ProxyEngine
import com.taran.app.domain.proxy.ProxyLog
import com.taran.app.domain.proxy.ProxyOrchestrator
import com.taran.app.domain.proxy.ProxyServiceLauncher
import com.taran.app.domain.proxy.ProxyStore
import com.taran.app.service.AndroidProxyServiceLauncher
import com.taran.app.domain.ssh.SSHManager
import com.taran.app.domain.server.ServerSetupRepository
import com.taran.app.domain.share.ShareRepository
import com.taran.app.domain.ssh.SshRepository
import com.taran.app.viewmodel.share.ImportViewModel
import com.taran.app.viewmodel.proxy.ProxyViewModel
import com.taran.app.viewmodel.server.ServerSetupViewModel
import com.taran.app.viewmodel.server.ServerViewModel
import com.taran.app.viewmodel.settings.SettingsViewModel
import com.taran.app.viewmodel.settings.BackupViewModel
import com.taran.app.viewmodel.server.ServerConfigViewModel
import com.taran.app.viewmodel.share.ShareViewModel
import com.taran.app.ui.util.AndroidHaptics
import com.taran.app.viewmodel.Haptics
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.io.File

val appModule = module {
    single { AppPreferences(androidContext()) }
    single { ProxyStore() }
    single { ProxyLog(File(androidContext().filesDir, "logs")) }
    single<ProxyServiceLauncher> { AndroidProxyServiceLauncher(androidContext(), get(), get()) }
    // Ядро одно на процесс: сессия переживает пересоздание сервиса.
    // noBackupFilesDir: состояние ядра приватное и не должно уезжать в облачный бэкап.
    single { ProxyEngine(androidContext().noBackupFilesDir.absolutePath, get(), get()) }
    // factory: каждому потребителю свой SSHManager - lastSeenFingerprint (TOFU) не должен
    // делиться между живой сессией и мастером/шарингом.
    factory { SSHManager() }
    single { SshRepository(androidContext(), get()) }
    single { AppUpdater(androidContext()) }
    single { BackupManager(get()) }
    single { ProxyOrchestrator(get(), get(), get()) }
    // factory: своя SSH-сессия на каждый прогон мастера, живой SshRepository не трогаем.
    factory { ServerSetupRepository(androidContext(), get()) }
    // factory по той же причине: SSH-операции шаринга не делят сессию с активным сервером.
    factory { ShareRepository(androidContext(), get()) }
    single { LinkImportBus() }
    single<Haptics> { AndroidHaptics(androidContext()) }

    viewModelOf(::ProxyViewModel)
    viewModelOf(::ServerViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ServerConfigViewModel)
    viewModelOf(::BackupViewModel)
    viewModelOf(::ServerSetupViewModel)
    viewModelOf(::ShareViewModel)
    viewModelOf(::ImportViewModel)
}
