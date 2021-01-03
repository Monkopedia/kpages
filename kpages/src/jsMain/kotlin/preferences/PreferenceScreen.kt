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
package com.monkopedia.kpages.preferences

import com.monkopedia.kpages.Navigator
import com.monkopedia.kpages.ViewControllerFactory
import kotlinx.css.paddingLeft
import kotlinx.css.paddingRight
import kotlinx.css.px
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.ReactElement
import react.dom.h1
import styled.css
import styled.styledDiv

actual class PreferenceScreen actual constructor(
    private val title: String,
    private val preferenceBuilder: PreferenceBuilder.(Navigator) -> Unit
) : ViewControllerFactory() {
    override val componentFactory: RBuilder.() -> ReactElement?
        get() = {
            child(PreferenceComponent::class) {
                attrs {
                    title = title
                    preferences = preferenceBuilder
                    navigator = Navigator.INSTANCE
                }
            }
        }
}

external interface PreferenceComponentProps : RProps {
    var preferences: ((PreferenceBuilder, Navigator) -> Unit)?
    var title: String?
    var navigator: Navigator?
}

class PreferenceComponent(props: PreferenceComponentProps) :
    RComponent<PreferenceComponentProps, RState>(props) {

    override fun RBuilder.render() {
        styledDiv {
            css {
                paddingLeft = 16.px
                paddingRight = 16.px
            }
            h1 {
                +(props.title ?: "")
            }
            props.preferences?.let {
                it(PreferenceBuilder(this), props.navigator!!)
            }
        }
    }
}
actual class PreferenceBuilder(val base: RBuilder)
