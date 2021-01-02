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

import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mSwitch
import com.ccfraser.muirwik.components.mTypography
import kotlinx.css.LinearDimension
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.px
import react.RBuilder
import react.RState
import react.setState
import styled.css

actual external interface SwitchPreferenceCategoryProps : PreferenceCategoryProps {
    actual var initialState: Boolean?
    actual var onChange: ((Boolean) -> Unit)?
}

external interface SwitchPreferenceCategoryState : RState {
    var selected: Boolean?
}

class SwitchPreferenceCategory :
    PreferenceCategory<SwitchPreferenceCategoryProps, SwitchPreferenceCategoryState>() {

    override fun RBuilder.renderHeader() {
        props.title?.let {
            mTypography(variant = MTypographyVariant.h5) {
                +it
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

actual inline fun PreferenceBuilder.switchPreferenceCategory(
    noinline handler: SwitchPreferenceCategoryProps.() -> Unit,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    base.child(SwitchPreferenceCategory::class) {
        attrs {
            handler()
            children = {
                it.builder()
            }
        }
    }
}

actual inline fun PreferenceBuilder.switchPreferenceCategory(
    title: String,
    initialState: Boolean,
    noinline onChange: ((Boolean) -> Unit)?,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    base.child(SwitchPreferenceCategory::class) {
        attrs {
            this.title = title
            this.initialState = initialState
            this.onChange = onChange
            children = {
                it.builder()
            }
        }
    }
}
