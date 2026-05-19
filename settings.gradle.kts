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

rootProject.name = "Bakti Marsada"
include(":app")
include(":core")
include(":ui")
include(":domain")
include(":data")
include(":firebase-core")
include(":data-source-cloudflare")
include(":data-source-python")
include(":data-source-firebase")
include(":data-source-simulate")
include(":feature-roulette")
