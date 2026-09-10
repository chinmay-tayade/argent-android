plugins {
    alias(libs.plugins.argent.jvm.library)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
