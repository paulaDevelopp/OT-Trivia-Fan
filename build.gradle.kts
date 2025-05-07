// Top-level build file where you can add configuration options common to all sub-projects/modules.
// build.gradle.kts (nivel raíz del proyecto)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}
