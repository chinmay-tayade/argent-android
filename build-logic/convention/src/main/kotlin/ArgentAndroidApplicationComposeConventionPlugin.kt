import com.android.build.api.dsl.ApplicationExtension
import com.argent.convention.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ArgentAndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        val extension = extensions.getByType(ApplicationExtension::class.java)
        configureAndroidCompose(extension)
    }
}
