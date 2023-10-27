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

import com.monkopedia.konstructor.frontend.utils.useEffect
import react.FC
import react.Props
import react.create
import react.router.PathRouteProps
import react.router.Route
import react.router.RouteProps
import react.router.Routes
import react.router.dom.BrowserRouter
import react.router.useLocation

external interface KPagesProps : Props {
    var app: KPagesApp?
    var title: Mutable<CharSequence>?
}

// external interface KPagesState : RState {
//    var title: CharSequence?
// }

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
val KPagesComponent = FC<KPagesProps> { props ->
    BrowserRouter {
        Routes {
            val routes = props.app?.routes ?: emptyList()
            for (route in routes) {
                Route {
                    if (route.exact) {
                        (this as PathRouteProps).path = route.path
                    } else {
                        (this as PathRouteProps).path = route.path + "/*"
                    }
                    element = FC<RouteProps> {
                        val currentTitle = Mutable((route.title ?: route.path) as CharSequence)
                        val location = useLocation()
                        props.title?.let { title ->
                            useEffect(title) {
                                currentTitle.collect(title::emit)
                            }
                        }
                        (route.factory as JsControllerFactory).element {
                            this.title = currentTitle
                            this.route = location.key
                        }
                    }.create()
                }
            }
        }
    }
}
