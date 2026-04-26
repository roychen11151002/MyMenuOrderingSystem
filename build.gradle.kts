// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
// roy modify for navigation & retrofit
    alias(libs.plugins.kotlin.serialization) apply false
// roy modify for Room
    alias(libs.plugins.kotlin.ksp) apply false
}