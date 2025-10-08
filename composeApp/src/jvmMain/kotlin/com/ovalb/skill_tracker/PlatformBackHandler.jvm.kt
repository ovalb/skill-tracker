package com.ovalb.skill_tracker

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(onBack: () -> Unit) {
    // Desktop has no system back button; no-op.
}
