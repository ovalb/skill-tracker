package com.ovalb.skill_tracker

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "skill_tracker",
    ) {
        App()
    }
}