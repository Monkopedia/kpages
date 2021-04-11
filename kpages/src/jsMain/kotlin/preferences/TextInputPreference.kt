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

import com.ccfraser.muirwik.components.MTypographyColor
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.dialog.mDialog
import com.ccfraser.muirwik.components.dialog.mDialogActions
import com.ccfraser.muirwik.components.dialog.mDialogContent
import com.ccfraser.muirwik.components.dialog.mDialogTitle
import com.ccfraser.muirwik.components.mTextField
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.targetInputValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import react.RBuilder
import react.RState
import react.dom.form
import react.setState

actual external interface TextInputPreferenceProps : PreferenceProps {
    actual var value: String?
    actual var dialogTitle: String?
    actual var dialogDescription: String?
    actual var hintText: String?
    actual var onChange: (suspend (String) -> Unit)?
}

external interface TextInputPreferenceState : RState {
    var dialogOpen: Boolean?
    var dialogValue: String?
}

class TextInputPreference : PreferenceBase<TextInputPreferenceProps, TextInputPreferenceState>() {
    override fun RBuilder.renderPreference() {
        props.title?.let {
            mTypography(
                it,
                MTypographyVariant.subtitle1,
                color = MTypographyColor.textPrimary
            )
        }
        props.subtitle?.let {
            mTypography(
                it,
                MTypographyVariant.body2,
                color = MTypographyColor.textSecondary
            )
        }
        mDialog(open = state.dialogOpen ?: false) {
            props.dialogTitle?.let {
                mDialogTitle(it)
            }
            mDialogContent {
                props.dialogDescription?.let {
                    mTypography {
                        +it
                    }
                }

                println("Binding text field ${props.value} ${state.dialogValue}")
                mTextField(
                    props.hintText ?: "Value",
                    defaultValue = props.value,
                    onChange = {
                        val value = it.targetInputValue
                        setState {
                            dialogValue = value
                        }
                    }) {
                }
            }
            mDialogActions {
                mButton("Cancel", onClick = {
                    setState {
                        dialogOpen = false
                    }
                }) {
                }
                mButton("OK", onClick = {
                    setState {
                        dialogOpen = false
                    }
                    GlobalScope.launch {
                        props.onChange?.let { it.invoke(state.dialogValue ?: "") }
                    }
                }) {

                }
            }
        }
    }
}

actual inline fun PreferenceBuilder.textInputPreference(
    noinline handler: TextInputPreferenceProps.() -> Unit
) {
    base.child(TextInputPreference::class) {
        attrs {
            onClick = {
                (it as TextInputPreference).setState {
                    dialogValue = it.props.value
                    dialogOpen = true
                }
            }
            handler()
        }
    }
}
