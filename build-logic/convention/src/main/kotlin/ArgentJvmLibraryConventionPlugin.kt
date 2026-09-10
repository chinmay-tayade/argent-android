import com.argent.convention.configureKotlinJvm
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

/** Pure Kotlin/JVM module — no Android. Used by `:core:domain`. */
class ArgentJvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("java-library")
            apply("org.jetbrains.kotlin.jvm")
        }
        configureKotlinJvm()

        dependencies {
            add("testImplementation", kotlin("test"))
        }
    }
}
