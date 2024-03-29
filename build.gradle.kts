/*
 * Copyright 2020 Jason Monk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-dev/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
}

plugins {
    alias(libs.plugins.autostyle)
    kotlin("multiplatform") version "1.9.20-Beta" apply false
}

allprojects {
    plugins.apply("com.github.autostyle")
    autostyle {
        kotlinGradle {
            // Since kotlin doesn't pick up on multi platform projects
            filter.include("**/*.kt")
            ktlint("0.39.0") {
                userData(mapOf("android" to "true"))
            }

            licenseHeader(
                """
                |Copyright 2020 Jason Monk
                |
                |Licensed under the Apache License, Version 2.0 (the "License");
                |you may not use this file except in compliance with the License.
                |You may obtain a copy of the License at
                |
                |    https://www.apache.org/licenses/LICENSE-2.0
                |
                |Unless required by applicable law or agreed to in writing, software
                |distributed under the License is distributed on an "AS IS" BASIS,
                |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                |See the License for the specific language governing permissions and
                |limitations under the License.""".trimMargin()
            )
        }
    }
}

subprojects {
    tasks.withType<KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xskip-prerelease-check"
        }
    }
}
