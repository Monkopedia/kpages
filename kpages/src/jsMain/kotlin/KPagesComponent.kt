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

import kotlin.reflect.KClass
import react.Component
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.router.dom.browserRouter
import react.router.dom.route
import react.router.dom.switch

external interface KPagesProps : RProps {
    var app: KPagesApp?
}

class KPagesComponent : RComponent<KPagesProps, RState>() {

    override fun RBuilder.render() {
        println("Routing: ${props.app}")
        browserRouter {
            switch {
                val routes = props.app?.routes ?: emptyList()
                for (route in routes) {
                    println("Route: $route")
                    route.factory.cls?.let {
                        route(
                            route.path,
                            it as KClass<out Component<RProps, *>>,
                            exact = route.exact
                        )
                    } ?: route<RProps>(route.path, exact = route.exact) {
                        route.factory.componentFactory(this)
                    }
                }
            }
        }
    }
}
