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
package com.monkopedia.kpages

import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.navigation.Screen
import java.lang.IllegalArgumentException

inline fun ViewControllerFactory(crossinline factory: (Navigator) -> Screen) =
    object : ViewControllerFactory() {
        override fun create(navigation: Navigator): Screen = factory(navigation)
    }

actual abstract class ViewControllerFactory {
    abstract fun create(navigation: Navigator): Screen
}

fun KPagesApp.navigator(navigation: Navigation): Navigator {
    return Navigator(this, navigation)
}

actual class Navigator(private val app: KPagesApp, internal val navigation: Navigation) {
    var paths = mutableListOf("/")
    actual val path: String
        get() = paths.last()

    actual suspend fun goBack() {
        paths.removeLast()
        navigation.popScreen()
    }

    actual suspend fun push(path: String) {
        val resolved = app.resolve(path)
            ?: throw IllegalArgumentException("No path for $path")
        paths.add(path)
        navigation.open(resolved.create(this))
    }
}
