package com.ovalb.skilltracker.server.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SkillController {
    // simple get endpoint that returns hello world

    @GetMapping("/")
    fun helloWorld(): String {
        return "hello world"
    }
}