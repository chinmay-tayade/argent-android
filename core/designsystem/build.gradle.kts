plugins {
    alias(libs.plugins.argent.android.library)
    alias(libs.plugins.argent.android.library.compose)
}

android {
    namespace = "com.argent.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.tooling.preview)
}
