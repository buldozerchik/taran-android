package com.taran.app.viewmodel

enum class HapticEvent { SUCCESS, ERROR, STEP }

fun interface Haptics {
    fun perform(event: HapticEvent)
}
