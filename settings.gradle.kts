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
        // Only used for com.github.nextcloud:Android-SingleSignOn, which
        // isn't published to Maven Central (see PLAN.md §3/§9 — Files-app SSO).
        maven("https://jitpack.io")
    }
}

rootProject.name = "TimeTracker"
include(":app")
