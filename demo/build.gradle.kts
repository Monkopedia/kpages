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
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target.COMMONJS

plugins {
    kotlin("multiplatform")
    application
}

version = "0.1"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlin-js-wrappers")
}

dependencies {
}

kotlin {
    js {
        useCommonJs()
        browser {
            webpackTask {
                output.libraryTarget = COMMONJS
            }
        }
        this.binaries.apply {
            executable()
        }
    }
    jvm {
        withJava()
    }
    sourceSets["commonMain"].dependencies {
        implementation(project(":kpages"))
    }
    sourceSets["jvmMain"].dependencies {
        implementation(project(":lanterna-ext"))
        implementation(libs.slf4j.api)
        implementation(libs.logback.classic)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.client.jetty)
        implementation(libs.kotlinx.coroutines.core)
    }
    sourceSets["jsMain"].dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(kotlin("stdlib-js"))
        implementation(libs.kotlin.extensions)
        implementation(libs.kotlin.css)
        implementation(libs.kotlin.emotion)
        implementation(libs.kotlin.styled.next)
        implementation(libs.kotlin.react)
        implementation(libs.kotlin.react.dom)
        implementation(libs.kotlin.react.router.dom)
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlin.mui)
        implementation(libs.kotlin.mui.icons)
        implementation(project(":kpages"))
        implementation(npm("codemirror", "5.58.3"))
        implementation(npm("showdown", "1.9.1"))
        implementation(npm("css-loader", "3.5.2"))
        implementation(npm("style-loader", "1.1.3"))
        implementation(npm("bootstrap", "^4.4.1"))
    }
    sourceSets["jsTest"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-test-js")
    }
}

application {
    mainClass.set("com.monkopedia.kpages.demo.DemoKt")
}
