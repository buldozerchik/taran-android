package com.taran.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.taran.app.R
import com.taran.app.data.config.Provider

@Composable
fun providerLabel(value: String): String = when (value) {
    Provider.RELAY -> stringResource(R.string.provider_relay)
    Provider.DIRECT -> stringResource(R.string.provider_direct)
    else -> value
}

@DrawableRes
fun providerIcon(value: String): Int = when (value) {
    Provider.RELAY -> R.drawable.conversion_path_24px
    else -> R.drawable.bolt_24px
}
