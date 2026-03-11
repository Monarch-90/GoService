pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "GoService"
include(":app")
include(":core")
include(":feature_schedule")
include(":feature_clients")
include(":feature_appointments")
include(":common_ui")
include(":navigation")
include(":feature_sidebar")
include(":feature_windows")
