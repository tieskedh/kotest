plugins {
   id("java")
   kotlin("multiplatform")
   id("java-library")
}

kotlin {
   targets {
      jvm()
   }

   sourceSets {
      val jvmMain by getting {
         dependencies {
            api(Libs.Kotlin.Compiler)
         }
      }
   }
}



tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
   kotlinOptions.jvmTarget = "1.8"
}

apply(from = "../../publish-mpp.gradle.kts")
