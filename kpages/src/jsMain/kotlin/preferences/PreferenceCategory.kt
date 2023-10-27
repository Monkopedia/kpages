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
import mui.material.Table
import mui.material.TableBody
import mui.material.Typography
import mui.material.styles.TypographyVariant
import react.FC
import react.dom.html.ReactHTML.div
import web.cssom.Auto
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.px

actual external interface PreferenceCategoryProps : PreferenceBaseProps {
    actual var title: String?
    actual var children: ((PreferenceBuilder) -> Unit)?
}

val PreferenceCategoryBase = FC<PreferencePropsHolder<PreferenceCategoryProps>> { props ->
    div {
        css {
            paddingLeft = 16.px
            paddingRight = 16.px
            width = Auto.auto
            display = Display.flex
            flexDirection = FlexDirection.row
        }
        +props.children
    }
    div {
        css {
            marginTop = 16.px
            marginBottom = 16.px
        }
        Table {
            TableBody {
                props.preferenceProps.children?.invoke(PreferenceBuilder(this))
            }
        }
    }
}

val PreferenceCategory = FC<PreferencePropsHolder<PreferenceCategoryProps>> { props ->
    PreferenceCategoryBase {
        preferenceProps = props.preferenceProps
        props.preferenceProps.title?.let {
            Typography {
                variant = TypographyVariant.h5
                +it
            }
        }
    }
}

actual inline fun PreferenceBuilder.preferenceCategory(
    crossinline handler: PreferenceCategoryProps.() -> Unit,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    base.apply {
        PreferenceCategory {
            preferenceProps = jso {
                handler()
                children = {
                    it.builder()
                }
            }
        }
    }
}

actual inline fun PreferenceBuilder.preferenceCategory(
    title: String,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    base.apply {
        PreferenceCategory {
            preferenceProps = jso {
                this.title = title
                children = {
                    it.builder()
                }
            }
        }
    }
}
