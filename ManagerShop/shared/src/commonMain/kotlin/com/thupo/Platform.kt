package com.thupo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform