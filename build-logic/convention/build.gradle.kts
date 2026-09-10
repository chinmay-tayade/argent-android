plugins {
    `kotlin-dsl`
}

group = "com.argent.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "argent.android.application"
            implementationClass = "ArgentAndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "argent.android.application.compose"
            implementationClass = "ArgentAndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "argent.android.library"
            implementationClass = "ArgentAndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "argent.android.library.compose"
            implementationClass = "ArgentAndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "argent.android.feature"
            implementationClass = "ArgentAndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "argent.android.hilt"
            implementationClass = "ArgentAndroidHiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = "argent.jvm.library"
            implementationClass = "ArgentJvmLibraryConventionPlugin"
        }
    }
}
