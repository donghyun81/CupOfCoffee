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
        maven("https://repository.map.naver.com/archive/maven")
    }
}

rootProject.name = "CupOfCoffee"
include(":app")
include(":core")
include(":core:database")
include(":core:data")
include(":core:network")
include(":sync")
include(":sync:work")
include(":core:common")
include(":core:datastore")
