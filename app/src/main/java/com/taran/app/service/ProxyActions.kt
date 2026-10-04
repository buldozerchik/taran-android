package com.taran.app.service

/**
 * Команды [ProxyService]. Ими же приходят внешние входы (тайл, виджет, ярлык,
 * кнопка в шторке) через [ProxyReceiver].
 */
object ProxyActions {
    const val START = "com.taran.app.START_PROXY"
    const val STOP = "com.taran.app.STOP_PROXY"
}
