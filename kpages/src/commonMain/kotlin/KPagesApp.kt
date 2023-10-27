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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmInline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

abstract class KPagesApp : CoroutineScope {
    internal val routes = mutableListOf<Route>()
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext + parentJob
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

    fun close() {
        parentJob.cancel()
    }
}

fun <T> Mutable(): MutableStateFlow<T?> = MutableStateFlow(null)

fun <T> Mutable(initial: T): MutableStateFlow<T> = MutableStateFlow(initial)

typealias Mutable<T> = MutableStateFlow<T>

@JvmInline
value class RouteBuilder(private val app: KPagesApp) {
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
