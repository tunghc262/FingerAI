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
        // astronomy engine (MIT) is only published on JitPack
        maven("https://jitpack.io") { content { includeGroup("com.github.cosinekitty") } }
    }
}
rootProject.name = "PalmReader"
include(":app")
