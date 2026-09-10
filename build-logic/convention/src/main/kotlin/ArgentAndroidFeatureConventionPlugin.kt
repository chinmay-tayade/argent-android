import com.android.build.api.dsl.LibraryExtension
import com.argent.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * Applied by `:feature:*` modules. Wires the standard feature stack: Android library +
 * Compose + Hilt + the core modules a feature always needs, plus a common test setup.
 */
class ArgentAndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("argent.android.library")
            apply("argent.android.library.compose")
            apply("argent.android.hilt")
        }

        extensions.configure<LibraryExtension> {
            defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        dependencies {
            add("implementation", project(":core:common"))
            add("implementation", project(":core:designsystem"))
            add("implementation", project(":core:domain"))

            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            add("implementation", libs.findLibrary("hilt-navigation-compose").get())
            add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())

            add("testImplementation", project(":core:testing"))
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
            add("testImplementation", libs.findLibrary("turbine").get())
            add("testImplementation", libs.findLibrary("mockk").get())

            add("androidTestImplementation", libs.findLibrary("androidx-test-ext-junit").get())
        }
    }
}
