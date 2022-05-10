import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation

plugins {
  kotlin("multiplatform")
  application
  kotlin("plugin.serialization") version "1.6.21"
}

group = "net.saff"
version = "1.0-SNAPSHOT"

kotlin {
  val jvmTarget = jvm()

  macosX64("native") {
    binaries {
      executable()
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("junit:junit:4.13.2")
        implementation("net.saff.checkmark:checkmark:0.1.3")
      }
    }
//    val nativeMain by getting {
//      dependsOn(commonMain)
//    }
//    val macosX64Main by getting {
//      dependsOn(nativeMain)
//    }
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
  extendsFrom(configurations["implementation"], configurations["runtimeOnly"])
}

artifacts {
  add("jvmJars", tasks.getByName("jvmJar"))
}

application {
  mainClass.set("net.saff.heap.ProfileKt")
}