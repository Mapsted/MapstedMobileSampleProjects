import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.sampledemoapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sampledemoapp"
        minSdk = 24
        targetSdk = 34
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
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/gradle/*"
            excludes += "/META-INF/*"}
    }
}
configurations {
    implementation{
        exclude(group = "org.jetbrains", module = "annotations")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Mapsted
    val mapstedSdkVersion = "6.0.1"
    implementation("com.mapsted:app-template:${mapstedSdkVersion}")
    implementation("com.mapsted:app-template-core:$mapstedSdkVersion")
    implementation("com.mapsted:sdk-loc-share:$mapstedSdkVersion")
    implementation("com.mapsted:sdk-loc-marketing:$mapstedSdkVersion")
    implementation("com.mapsted:sdk-ui-components:$mapstedSdkVersion")
    implementation("com.mapsted:sdk-map-ui:$mapstedSdkVersion")
    implementation("com.mapsted:sdk-map:$mapstedSdkVersion")
//    implementation("com.mapsted:sdk-alerts:$mapstedSdkVersion")
    implementation("com.mapsted:sdk-inapp-notification:$mapstedSdkVersion")
    implementation("com.mapsted:sdk-geofence:$mapstedSdkVersion")
    implementation("com.mapsted:sdk-geofence-offline:$mapstedSdkVersion")
    implementation("com.mapsted:sdk-core:$mapstedSdkVersion")
}