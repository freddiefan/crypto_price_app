package com.example.cyptopricetracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform