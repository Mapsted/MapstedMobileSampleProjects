

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
    implementation (libs.firebase.messaging.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Mapsted
    implementation(libs.app.template)
    implementation(libs.app.template.core)
    implementation(libs.sdk.loc.share)
    implementation(libs.sdk.loc.marketing)
    implementation (libs.sdk.alerts)
    implementation(libs.sdk.ui.components)
    implementation(libs.sdk.map.ui)
    implementation(libs.sdk.map)
    implementation(libs.sdk.inapp.notification)
    implementation(libs.sdk.geofence)
    implementation(libs.sdk.geofence.offline)
    implementation(libs.sdk.core)
    implementation(libs.sdk.topbar.notifications)
}