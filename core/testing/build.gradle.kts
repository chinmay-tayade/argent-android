plugins {
    alias(libs.plugins.argent.android.library)
}

android {
    namespace = "com.argent.core.testing"
}

dependencies {
    api(libs.junit4)
    api(libs.turbine)
    api(libs.kotlinx.coroutines.test)
    api(project(":core:domain"))
}
