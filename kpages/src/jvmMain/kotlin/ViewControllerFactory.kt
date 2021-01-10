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
        override fun create(navigation: Navigator, title: Mutable<CharSequence>): Screen = factory(navigation)
    }

actual abstract class ViewControllerFactory {
    abstract fun create(navigation: Navigator, title: Mutable<CharSequence>): Screen
}

fun KPagesApp.navigator(navigation: Navigation): Navigator {
    return Navigator(this, navigation)
}

actual class Navigator(private val app: KPagesApp, internal val navigation: Navigation) {
    var paths = mutableListOf("/")
    var titles = mutableListOf(Mutable("" as CharSequence))
    actual val path: String
        get() = paths.last()
    private val selector = SelectingMutable(titles.last())
    val title = selector.output

    actual suspend fun goBack() {
        paths.removeLast()
        titles.removeLast()
        selector.source = titles.last()
        navigation.popScreen()
    }

    actual suspend fun push(path: String) {
        val resolved = app.resolve(path)
            ?: throw IllegalArgumentException("No path for $path")
        val title = Mutable((resolved.title ?: resolved.path) as CharSequence)
        titles.add(title)
        paths.add(path)
        val screen = resolved.factory.create(this, title)
        selector.source = title
        navigation.open(screen)
    }
}

class SelectingMutable<T>(initial: Mutable<T>) {
    private val listener: (T) -> Unit = {
        output.value = it
    }
    var source = initial
        set(value) {
            field.detach(listener)
            value.attach(listener)
        }
    val output = Mutable(initial.value)

    init {
        initial.attach(listener)
    }
}