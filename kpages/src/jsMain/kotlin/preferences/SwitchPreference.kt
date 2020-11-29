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
import com.ccfraser.muirwik.components.mSwitch
import com.ccfraser.muirwik.components.mTypography
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

actual external interface SwitchPreferenceProps : PreferenceProps {
    actual var initialState: Boolean?
    actual var onChange: ((Boolean) -> Unit)?
}

actual external interface SwitchPreferenceState : RState {
    actual var selected: Boolean?
}

class SwitchPreference : PreferenceBase<SwitchPreferenceProps, SwitchPreferenceState>() {
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
            mSwitch(
                state.selected ?: props.initialState ?: false,
                onChange = { e, b ->
                    setState {
                        selected = b
                        props.onChange?.invoke(b)
                    }
                }
            ) {
                css {
                    marginLeft = LinearDimension.auto
                    marginRight = 16.px
                }
            }
        }
    }
}

actual inline fun PreferenceBuilder.switchPreference(
    noinline handler: SwitchPreferenceProps.() -> Unit
) {
    base.child(SwitchPreference::class) {
        attrs {
            onClick = {
                (it as SwitchPreference).setState {
                    selected = !(selected ?: it.props.initialState ?: false)
                    it.props.onChange?.invoke(selected!!)
                }
            }
            handler()
        }
    }
}
