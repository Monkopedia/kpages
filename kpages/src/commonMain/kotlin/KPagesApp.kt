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

import com.monkopedia.kpages.KPagesApp.Route

abstract class KPagesApp {
    internal val routes = mutableListOf<Route>()
    abstract fun RouteBuilder.routes()

    fun resolve(path: String): Route? {
        for (r in routes) {
            if (r.exact) {
                if (path == r.path) {
                    return r
                }
            } else {
                if (path.startsWith(r.path)) {
                    return r
                }
            }
        }
        return null
    }

    init {
        @Suppress("LeakingThis")
        RouteBuilder(this).routes()
    }

    data class Route(
        val path: String,
        val exact: Boolean,
        val title: String?,
        val factory: ViewControllerFactory
    )
}

fun <T> Mutable(): Mutable<T?> = Mutable(null)

class Mutable<T>(initial: T) {
    var value: T = initial
        set(value) {
            if (field != value) {
                field = value
                callbacks.forEach {
                    it(value)
                }
            }
        }
    private val callbacks = mutableListOf<(T) -> Unit>()

    fun attach(callback: (T) -> Unit) {
        callback(value)
        callbacks.add(callback)
    }

    fun detach(callback: (T) -> Unit) {
        callbacks.remove(callback)
    }
}


inline class RouteBuilder(private val app: KPagesApp) {
    fun route(s: String, title: String?, cls: ViewControllerFactory) {
        app.routes.add(Route(s, true, title, cls))
    }

    fun prefixRoute(s: String, title: String?, cls: ViewControllerFactory) {
        app.routes.add(Route(s, false, title, cls))
    }

    fun route(s: String, cls: ViewControllerFactory) {
        app.routes.add(Route(s, true, null, cls))
    }

    fun prefixRoute(s: String, cls: ViewControllerFactory) {
        app.routes.add(Route(s, false, null, cls))
    }
}
