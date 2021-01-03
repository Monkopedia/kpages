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

    fun resolve(path: String): ViewControllerFactory? {
        for (r in routes) {
            if (r.exact) {
                if (path == r.path) {
                    return r.factory
                }
            } else {
                if (path.startsWith(r.path)) {
                    return r.factory
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
        val factory: ViewControllerFactory
    )
}

inline class RouteBuilder(private val app: KPagesApp) {
    fun route(s: String, cls: ViewControllerFactory) {
        app.routes.add(Route(s, true, cls))
    }

    fun prefixRoute(s: String, cls: ViewControllerFactory) {
        app.routes.add(Route(s, false, cls))
    }
}
