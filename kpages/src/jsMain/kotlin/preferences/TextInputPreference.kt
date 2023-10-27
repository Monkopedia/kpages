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

import js.core.jso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mui.material.Button
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.TextField
import mui.material.Typography
import mui.system.sx
import react.FC
import react.create
import react.dom.onChange
import react.useState
import web.cssom.px

actual external interface TextInputPreferenceProps : PreferenceProps {
    actual var value: String?
    actual var dialogTitle: String?
    actual var dialogDescription: String?
    actual var hintText: String?
    actual var onChange: (suspend (String) -> Unit)?
}

val TextInputPreference = FC<PreferencePropsHolder<TextInputPreferenceProps>> { props ->
    var dialogOpen: Boolean by useState(false)
    var dialogValue: String? by useState(props.preferenceProps.value)
    PreferenceBase {
        preferenceProps = props.preferenceProps
        preferenceProps.onClick = {
            dialogValue = props.preferenceProps.value
            dialogOpen = true
        }
        PreferenceText {
            +props
        }
        Dialog {
            open = dialogOpen
            props.preferenceProps.dialogTitle?.let {
                DialogTitle {
                    +it
                }
            }
            DialogContent {
                props.preferenceProps.dialogDescription?.let {
                    Typography {
                        +it
                    }
                }

                TextField {
                    sx {
                        marginTop = 32.px
                    }
                    label = Typography.create {
                        +(props.preferenceProps.hintText ?: "Value")
                    }
                    defaultValue = props.preferenceProps.value
                    onChange = {
                        val value = it.target.asDynamic().value.toString()
                        dialogValue = value
                    }
                }
            }
            DialogActions {
                Button {
                    +"Cancel"
                    onClick = {
                        dialogOpen = false
                    }
                }
                Button {
                    +"OK"
                    onClick = {
                        dialogOpen = false
                        GlobalScope.launch {
                            props.preferenceProps.onChange?.invoke(dialogValue ?: "")
                        }
                    }
                }
            }
        }
    }
}

actual inline fun PreferenceBuilder.textInputPreference(
    noinline handler: TextInputPreferenceProps.() -> Unit
) {
    base.apply {
        TextInputPreference {
            preferenceProps = jso {
                handler()
            }
        }
    }
}
