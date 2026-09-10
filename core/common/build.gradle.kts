plugins {
    alias(libs.plugins.argent.android.library)
    alias(libs.plugins.argent.android.hilt)
}

android {
    namespace = "com.argent.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
