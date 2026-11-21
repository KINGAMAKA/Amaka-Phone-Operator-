plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.amaka.phoneoperator"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.amaka.phoneoperator"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}
