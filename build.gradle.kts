// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.23")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}