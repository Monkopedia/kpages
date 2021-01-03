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

import com.monkopedia.kpages.ClassFactory
import com.monkopedia.kpages.ViewControllerFactory
import kotlinx.browser.window
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.a
import react.dom.div

external interface RootState : RState {
    var search: String?
}

actual val RootDemoFactory: ViewControllerFactory = ClassFactory(ImdexRoot::class)

class ImdexRoot : RComponent<RProps, RootState>() {
    override fun RBuilder.render() {
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
            attrs {
                onClickFunction = {
                    window.open("/preference_demo", "_self")
                }
            }
        }
//                        }
//                    }
//                }
//            }
//        }
    }
}
