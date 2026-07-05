pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Kindred"

include(":app")
include(":core:ui")
include(":core:data")
include(":feature:auth")
include(":feature:profile")
include(":feature:discovery")
include(":feature:chat")
