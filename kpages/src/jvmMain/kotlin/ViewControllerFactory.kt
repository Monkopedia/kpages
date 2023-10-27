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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

inline fun ViewControllerFactory(crossinline factory: (Navigator, String) -> Screen) =
    object : JvmControllerFactory() {
        override fun create(
            navigation: Navigator,
            path: String,
            title: Mutable<CharSequence>
        ): Screen = factory(navigation, path)
    }

abstract class JvmControllerFactory : ViewControllerFactory() {
    internal abstract fun create(
        navigation: Navigator,
        path: String,
        title: Mutable<CharSequence>
    ): Screen
}

fun KPagesApp.navigator(navigation: Navigation, title: Mutable<CharSequence>): Navigator {
    return NavigatorImpl(this, navigation, title)
}

internal class NavigatorImpl(
    private val app: KPagesApp,
    val navigation: Navigation,
    title: Mutable<CharSequence>
) : Navigator {
    var paths = mutableListOf("/")
    var titles = mutableListOf(Mutable("" as CharSequence))
    override val path: String
        get() = paths.last()
    private val selector = SelectingMutable(titles.last())

    init {
        app.launch {
            selector.output.collect(title::emit)
        }
    }

    override suspend fun goBack() {
        paths.removeLast()
        titles.removeLast()
        selector.source = titles.last()
        navigation.popScreen()
    }

    override suspend fun push(path: String) {
        val resolved = app.resolve(path)
            ?: throw IllegalArgumentException("No path for $path")
        val title = Mutable((resolved.title ?: resolved.path) as CharSequence)
        titles.add(title)
        paths.add(path)
        val screen = (resolved.factory as JvmControllerFactory).create(this, path, title)
        selector.source = title
        navigation.open(screen)
    }
}

class SelectingMutable<T>(
    initial: Mutable<T>,
) {
    private val sourceField = Mutable(initial)
    var source by sourceField::value

    @OptIn(ExperimentalCoroutinesApi::class)
    val output = sourceField.flatMapLatest { it }
}
