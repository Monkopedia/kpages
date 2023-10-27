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
package com.monkopedia.kpages.demo

import com.monkopedia.kpages.ViewControllerFactory
import com.monkopedia.kpages.factory
import kotlinx.browser.window
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2

external interface RootProps : Props {
    var path: String?
}

actual val RootDemoFactory: ViewControllerFactory = factory { props ->
    ImdexRoot {
        this.path = props.route
    }
}

val ImdexRoot = FC<RootProps> { props ->
//        mThemeProvider(theme = theme) {
//            styledDiv {
//                css {
//                    marginTop = LinearDimension("70px")
//                    height = 100.vh - 70.px
//                    overflowY = Overflow.auto
//                }
//
//                browserRouter {
//                    switch {
//                        // route("/", SearchScreen::class, exact = true)
//                        route<RProps>("/theme_demo", exact = false) { props ->
//                            div {
//                                child(ThemeDemo::class) {
//
//                                }
//                            }
//                        }
//                        route("/preference_demo", PreferenceDemoScreen::class, exact = true)
//                        route("/", exact = false) {
    div {
        +"Page: ${window.location}"
    }
    a {
        +"Next page"
        onClick = {
            window.open("/preference_demo", "_self")
        }
    }
    br { }
    h2 {
        +"Path: ${props.path}"
    }

//                        }
//                    }
//                }
//            }
//        }
}
