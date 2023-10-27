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

import emotion.react.css
import js.core.jso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mui.material.Switch
import react.FC
import react.dom.html.ReactHTML.div
import react.useState
import web.cssom.AlignItems
import web.cssom.Auto
import web.cssom.Display
import web.cssom.px

actual external interface SwitchPreferenceProps : PreferenceProps {
    actual var initialState: Boolean?
    actual var onChange: (suspend (Boolean) -> Unit)?
}

val SwitchPreference = FC<PreferencePropsHolder<SwitchPreferenceProps>> { props ->
    var selected: Boolean by useState(props.preferenceProps.initialState ?: false)
    PreferenceBase {
        preferenceProps = props.preferenceProps
        preferenceProps.onClick = {
            val newValue = !selected
            selected = newValue
            GlobalScope.launch {
                props.preferenceProps.onChange?.invoke(newValue)
            }
        }
        div {
            css {
                display = Display.flex
                alignItems = AlignItems.center
            }
            div {
                PreferenceText {
                    +props
                }
            }
            Switch {
                this.checked = selected
                onChange = { e, b ->
                    selected = b
                    GlobalScope.launch {
                        props.preferenceProps.onChange?.invoke(b)
                    }
                }
                css {
                    marginLeft = Auto.auto
                    marginRight = 16.px
                }
            }
        }
    }
}

actual inline fun PreferenceBuilder.switchPreference(
    noinline handler: SwitchPreferenceProps.() -> Unit
) {
    base.apply {
        SwitchPreference {
            preferenceProps = jso {
                handler()
            }
        }
    }
}
