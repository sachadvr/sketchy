pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.2.2" apply false
        id("org.jetbrains.kotlin.android") version "1.9.22" apply false
        id("com.google.dagger.hilt.android") version "2.50" apply false
        id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
        kotlin("plugin.serialization") version "1.9.22" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}

rootProject.name = "My Application"
include(":app")
