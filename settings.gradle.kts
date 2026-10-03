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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "offlinefirstapp"

include(":app")
include(":core:common")
include(":core:domain")
include(":core:database")
include(":core:network")
include(":core:data")
include(":core:sync")
include(":core:designsystem")
include(":feature:home")
include(":feature:accounts")
include(":feature:transactions")
include(":feature:sync")
