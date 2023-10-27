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
import mui.material.MenuItem
import mui.material.Select
import react.FC
import react.dom.html.ReactHTML.div
import react.useState
import web.cssom.AlignItems
import web.cssom.Auto
import web.cssom.Display
import web.cssom.px

actual external interface SelectionOption {
    actual var label: String
}

actual external interface SelectionPreferenceProps<T : SelectionOption> : PreferenceProps {
    actual var initialState: T?
    actual var onChange: (suspend (T?) -> Unit)?
    actual var options: List<T>?
}

fun <T : SelectionOption> SelectionPreference() =
    FC<PreferencePropsHolder<SelectionPreferenceProps<T>>> { props ->
        var selection: T? by useState()
        PreferenceBase {
            preferenceProps = props.preferenceProps
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
                Select {
                    this.value = props.preferenceProps.options?.indexOf(
                        selection
                            ?: props.preferenceProps.initialState
                    ).toString()
                    onChange = { e, c ->
                        console.log(e)
                        console.log(c)
                        selection = props.preferenceProps.options?.get(
                            e.target.value.toInt()
                        )
                        GlobalScope.launch {
                            props.preferenceProps.onChange?.invoke(selection)
                        }
                    }
                    css {
                        marginLeft = Auto.auto
                        marginRight = 16.px
                    }
                    val list = props.preferenceProps.options ?: emptyList()
                    for ((index, option) in list.withIndex()) {
                        MenuItem {
                            +option.label
                            this.value = index.toString()
                        }
                    }
                }
            }
        }
    }

actual inline fun <reified T : SelectionOption> PreferenceBuilder.selectionPreference(
    noinline handler: SelectionPreferenceProps<T>.() -> Unit
) {
    val pref = SelectionPreference<T>()
    base.apply {
        pref {
            preferenceProps = jso {
                handler()
            }
        }
    }
}
