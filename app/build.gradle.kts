plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.practicalvideorecoding"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.practicalvideorecoding"
        minSdk = 26
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation ("androidx.fragment:fragment-ktx:1.6.2") // or later
    implementation ("androidx.activity:activity-ktx:1.8.2") // or later


    implementation ("androidx.camera:camera-camera2:1.3.1")
// CameraX Lifecycle Library
    implementation ("androidx.camera:camera-lifecycle:1.3.1")
// CameraX View class
    implementation ("androidx.camera:camera-view:1.3.1")
    //CameraX videoCapture library.  version 1.2 changes the video capture a lot and I don't see an example to fix this code with yet.
    implementation ("androidx.camera:camera-video:1.3.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}