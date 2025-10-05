package com.ovalb.skill_tracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform