plugins {
   id("java")
   kotlin("jvm")
   id("java-library")
   id("java-gradle-plugin")
   id("com.gradle.plugin-publish").version(Libs.GradlePluginPublishVersion)
}

dependencies {
   api(Libs.Kotlin.Gradle)
}

tasks {
   pluginBundle {
      website = "http://kotest.io"
      vcsUrl = "https://github.com/kotest"
      tags = listOf("kotest", "kotlin", "testing", "integrationTesting")
   }
   gradlePlugin {
      plugins {
         create("kotestAssertionsPlugin") {
            id = "io.kotest.assertions"
            implementationClass = "io.kotest.asssertions.gradle.plugin.TestKotlinGradleSubplugin"
            displayName = "Gradle Kotest Assertions Plugin"
            description = "Adds support to Kotlin for better assertion messages"
         }
      }
   }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
   kotlinOptions.jvmTarget = "1.8"
}
apply(from = "../../publish-mpp.gradle.kts")
