package org.example.skikosize

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform