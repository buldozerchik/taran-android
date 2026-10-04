@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.taran.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.taran.app.ui.theme.LocalReducedMotion

/** Wavy-индикатор; при reduced motion - обычная полоса без волновой анимации. */
@Composable
fun BusyProgressIndicator(modifier: Modifier = Modifier) {
    if (LocalReducedMotion.current) {
        LinearProgressIndicator(modifier = modifier.fillMaxWidth())
    } else {
        LinearWavyProgressIndicator(modifier = modifier.fillMaxWidth())
    }
}

/** Determinate-вариант ([progress] 0..1) с тем же reduced-motion фолбэком. */
@Composable
fun BusyProgressIndicator(progress: () -> Float, modifier: Modifier = Modifier) {
    if (LocalReducedMotion.current) {
        LinearProgressIndicator(progress = progress, modifier = modifier.fillMaxWidth())
    } else {
        LinearWavyProgressIndicator(progress = progress, modifier = modifier.fillMaxWidth())
    }
}
