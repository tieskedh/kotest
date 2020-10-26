package io.kotest.asssertions.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class KotestGradleSubplugin : Plugin<Project> {
   companion object {
      fun isEnabled(project: Project): Boolean {
         return project.plugins.findPlugin(TestKotlinGradleSubplugin::class.java) != null
      }
   }

   override fun apply(project: Project) {
   }
}

class TestKotlinGradleSubplugin : KotlinCompilerPluginSupportPlugin {
   companion object {
      const val ARTIFACT_GROUP_NAME = "io.kotest"
      const val ARTIFACT_NAME = "kotest-assertions-compiler-plugin-jvm"
      const val ARTIFACT_SHADED_NAME = "kotest-assertions-compiler-plugin-shaded"
      const val ARTIFACT_VERSION = "4.4.0-LOCAL"
      const val PLUGIN_ID = "io.kotest.kotest-assertions-compiler-plugin-jvm"
   }

   override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
      KotestGradleSubplugin.isEnabled(kotlinCompilation.target.project)

   override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
      val project = kotlinCompilation.target.project
//      return project.provider {
//         listOf(SubpluginOption(key = "optio-key", value = "option-value"))
//      }
      return project.provider {
         emptyList()
      }
   }

   override fun getPluginArtifact(): SubpluginArtifact =
      SubpluginArtifact(ARTIFACT_GROUP_NAME, ARTIFACT_NAME, ARTIFACT_VERSION)

   override fun getPluginArtifactForNative(): SubpluginArtifact? = null

   override fun getCompilerPluginId() = PLUGIN_ID
}
