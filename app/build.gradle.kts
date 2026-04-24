plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
// roy modify for navigation & retrofit
    alias(libs.plugins.kotlin.serialization)
// roy modify for Room
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.example.mymenuorderingsystem"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.mymenuorderingsystem"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
// roy modify for flavor & Timber
        buildConfig = true
    }
// roy modify for flavor
    flavorDimensions.add("customer_group")

    productFlavors {
        create("HyperNet") {
            dimension = "customer_group"
            buildConfigField("String", "API_URL", "\"https://hypernet.com.tw\"")
            buildConfigField("String", "API_NAME", "\"HyperNet\"")
            buildConfigField("int", "API_MODE", "10")
            buildConfigField("boolean", "API_SHOW", "true")
        }

        create("iMageTech") {
            dimension = "customer_group"
            buildConfigField("String", "API_URL", "\"https://imagetech.com.tw\"")
            buildConfigField("String", "API_NAME", "\"iMageTech\"")
            buildConfigField("int", "API_MODE", "60")
            buildConfigField("boolean", "API_SHOW", "false")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
// roy modify for Icon
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
// roy modify for Timber
    implementation(libs.timber)
// roy modify for navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
// roy modify for Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
// roy modify for workManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.koin.androidx.workmanager)
// roy modify for Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
// roy modify for retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}