import com.android.build.api.dsl.ApplicationExtension
import com.argent.convention.configureKotlinAndroid
import com.argent.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ArgentAndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.application")
            apply("org.jetbrains.kotlin.android")
        }

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
        }
    }
}
