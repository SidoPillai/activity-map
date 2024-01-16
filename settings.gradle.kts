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
        maven("https://esri.jfrog.io/artifactory/arcgis")
        maven("https://jitpack.io")
    }
}

rootProject.name = "Activity Map"
include(":app")
 