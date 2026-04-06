plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.offlinelifesaver"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.offlinelifesaver"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // Offline Connectivity (Mesh)
    implementation("com.google.android.gms:play-services-nearby:18.7.0")
    
    // Offline Map Library
    implementation("org.osmdroid:osmdroid-android:6.1.18")
}
