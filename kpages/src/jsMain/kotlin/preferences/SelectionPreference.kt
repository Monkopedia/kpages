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
import com.ccfraser.muirwik.components.mSelect
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ccfraser.muirwik.components.targetValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.Display
import kotlinx.css.LinearDimension
import kotlinx.css.display
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.px
import react.RBuilder
import react.RState
import react.setState
import styled.css
import styled.styledDiv

actual external interface SelectionOption {
    actual var label: String
}

actual external interface SelectionPreferenceProps<T : SelectionOption> : PreferenceProps {
    actual var initialState: T?
    actual var onChange: (suspend (T?) -> Unit)?
    actual var options: List<T>?
}

external interface SelectionPreferenceState<T : SelectionOption> : RState {
    var selection: T?
}

class SelectionPreference<T : SelectionOption> :
    PreferenceBase<SelectionPreferenceProps<T>, SelectionPreferenceState<T>>() {
    override fun RBuilder.renderPreference() {
        styledDiv {
            css {
                display = Display.flex
            }
            styledDiv {
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
            }
            mSelect(
                props.options?.indexOf(state.selection ?: props.initialState),
                onChange = { e, c ->
                    setState {
                        selection = props.options?.get(e.targetValue.toString().toInt())
                        GlobalScope.launch {
                            props.onChange?.invoke(selection)
                        }
                    }
                }
            ) {
                css {
                    marginLeft = LinearDimension.auto
                    marginRight = 16.px
                }
                val list = props.options ?: emptyList()
                for ((index, option) in list.withIndex()) {
                    mMenuItem(option.label, value = index.toString())
                }
            }
        }
    }
}

actual inline fun <reified T : SelectionOption> PreferenceBuilder.selectionPreference(
    noinline handler: SelectionPreferenceProps<T>.() -> Unit
) {
    base.child<SelectionPreferenceProps<T>, SelectionPreference<T>> {
        attrs(handler)
    }
}
