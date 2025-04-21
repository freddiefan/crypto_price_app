package com.example.cyptopricetracker

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Testing Testing Hello, ${platform.name}!"
    }
}