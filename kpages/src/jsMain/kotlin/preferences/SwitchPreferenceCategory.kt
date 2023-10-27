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
import mui.material.Typography
import mui.material.styles.TypographyVariant
import react.FC
import react.useState
import web.cssom.Auto
import web.cssom.px

actual external interface SwitchPreferenceCategoryProps : PreferenceCategoryProps {
    actual var initialState: Boolean?
    actual var onChange: (suspend (Boolean) -> Unit)?
}

val SwitchPreferenceCategory = FC<PreferencePropsHolder<SwitchPreferenceCategoryProps>> { props ->
    var selected: Boolean? by useState(props.preferenceProps.initialState)
    PreferenceCategoryBase {
        preferenceProps = props.preferenceProps
        props.preferenceProps.title?.let {
            Typography {
                variant = TypographyVariant.h5
                +it
            }
        }
        Switch {
            checked = selected
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

actual inline fun PreferenceBuilder.switchPreferenceCategory(
    noinline handler: SwitchPreferenceCategoryProps.() -> Unit,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    base.apply {
        SwitchPreferenceCategory {
            preferenceProps = jso {
                handler()
                children = {
                    it.builder()
                }
            }
        }
    }
}

actual inline fun PreferenceBuilder.switchPreferenceCategory(
    title: String,
    initialState: Boolean,
    noinline onChange: (suspend (Boolean) -> Unit)?,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    base.apply {
        SwitchPreferenceCategory {
            preferenceProps = jso {
                this.title = title
                this.initialState = initialState
                this.onChange = onChange
                children = {
                    it.builder()
                }
            }
        }
    }
}
