/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation

plugins {
  kotlin("multiplatform")
  // application
  kotlin("plugin.serialization")
  id("maven-publish")
}

// Currently can publish to mavenLocal as net.saff:befuzz-jvm:0.1.1
group = "net.saff"
version = "0.1.1"

kotlin {
  val jvmTarget = jvm {
  }

  // SAFF: warnings
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlinx.serialization.json)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.kotlinx.serialization.json)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.junit)
        implementation(libs.checkmark)
      }
    }
    val nativeMain by creating
  }

  // Thanks to https://github.com/jmfayard/kotlin-cli-starter/blob/5201ee91122b4572d40167e7fbfae2f341ce5dfb/build.gradle.kts#L139
  // Without whom I'd have never found this...
  tasks.withType<JavaExec> {
    // code to make run task in kotlin multiplatform work
    val compilation =
      jvmTarget.compilations.getByName<KotlinJvmCompilation>("main")

    val classes = files(
      compilation.runtimeDependencyFiles,
      compilation.output.allOutputs
    )
    classpath(classes)
  }
}

val jvmJars: Configuration by configurations.creating {
  isCanBeConsumed = true
  isCanBeResolved = false
  extendsFrom(configurations["jvmMainImplementation"])
}

artifacts {
  add("jvmJars", tasks.getByName("jvmJar"))
}

kotlin {
  jvmToolchain(17)
}

// SAFF:
// w: The Kotlin source set nativeMain was configured but not added to any Kotlin compilation.
// You can add a source set to a target's compilation by connecting it with the compilation's default source set using 'dependsOn'.
// See https://kotl.in/connecting-source-sets