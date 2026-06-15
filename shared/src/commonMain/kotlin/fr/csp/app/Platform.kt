package fr.csp.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform