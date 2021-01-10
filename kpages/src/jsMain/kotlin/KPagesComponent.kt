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

import react.RBuilder
import react.RProps
import react.RState
import react.router.dom.browserRouter
import react.router.dom.route
import react.router.dom.switch

external interface KPagesProps : RProps {
    var app: KPagesApp?
    var title: Mutable<CharSequence>?
}

external interface KPagesState : RState {
    var title: CharSequence?
}

class KPagesComponent : LifecycleComponent<KPagesProps, KPagesState>() {
    private val listenerHolder = LifecycleHolder().also {
        registerLifecycleAware(it)
    }

    override fun RBuilder.render() {
        browserRouter {
            switch {
                val routes = props.app?.routes ?: emptyList()
                for (route in routes) {
                    val factory = route.factory
                    route<RProps>(route.path, exact = route.exact) {
                        val currentTitle = Mutable((route.title ?: route.path) as CharSequence)
                        with(factory) {
                            create(it.location.pathname, currentTitle).also {
                                setTitle(currentTitle)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setTitle(title: Mutable<CharSequence>) {
        val mutableTitle = props.title ?: return
        listenerHolder.lifecycle = title.onValue {
            mutableTitle.value = it
        }
    }
}
