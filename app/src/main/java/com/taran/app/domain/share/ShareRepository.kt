package com.taran.app.domain.share

import android.content.Context
import com.taran.app.data.config.SshConfig
import com.taran.app.data.control.ClientData
import com.taran.app.data.control.ClientListData
import com.taran.app.data.control.decodeBase64
import com.taran.app.data.server.ServerBackend
import com.taran.app.data.share.ShareInfo
import com.taran.app.data.share.SharedClient
import com.taran.app.domain.server.ServerCommand
import com.taran.app.domain.server.ServerControl
import com.taran.app.domain.server.asUnit
import com.taran.app.domain.server.requireData
import com.taran.app.domain.ssh.SSHManager

class ShareRepository(context: Context, ssh: SSHManager) {

    private val control = ServerControl(context, ssh)

    data class Snapshot(val info: ShareInfo, val clients: List<SharedClient>)

    /** [wgConf] null - у клиента нет пира (чужой VPN): в ссылку идёт только taran-часть. */
    data class Access(val client: SharedClient, val wgConf: String?)

    suspend fun list(cfg: SshConfig): Result<Snapshot> =
        control.run(cfg, ServerCommand.ClientList)
            .requireData<ClientListData>()
            .map { d ->
                val s = d.share
                Snapshot(
                    info = ShareInfo(
                        mode = s.mode,
                        obfProfile = s.obfProfile,
                        obfKey = s.obfKey,
                        wgBackend = s.backend == ServerBackend.NEW
                    ),
                    clients = SharedClient.list(d)
                )
            }

    suspend fun add(cfg: SshConfig, name: String): Result<Access> =
        control.run(cfg, ServerCommand.ClientAdd(name)).requireData<ClientData>().map(::access)

    suspend fun conf(cfg: SshConfig, name: String): Result<Access> =
        control.run(cfg, ServerCommand.ClientConf(name)).requireData<ClientData>().map(::access)

    suspend fun remove(cfg: SshConfig, name: String): Result<Unit> =
        control.run(cfg, ServerCommand.ClientRemove(name)).asUnit()

    private fun access(d: ClientData) = Access(SharedClient.from(d.client), decodeBase64(d.confB64))

    companion object {
        /** = valid_name в install.sh: имя - ключ на сервере и имя файлов клиента. */
        private val NAME = Regex("^[A-Za-z0-9][A-Za-z0-9._-]{0,31}$")

        fun isValidName(name: String): Boolean = NAME.matches(name)
    }
}
