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
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.table.mTable
import com.ccfraser.muirwik.components.table.mTableBody
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.marginBottom
import kotlinx.css.marginTop
import kotlinx.css.paddingLeft
import kotlinx.css.paddingRight
import kotlinx.css.px
import kotlinx.css.width
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import styled.styledDiv

actual external interface PreferenceCategoryProps : RProps {
    actual var title: String?
    actual var children: ((PreferenceBuilder) -> Unit)?
}

open class PreferenceCategory<P : PreferenceCategoryProps, S : RState> : RComponent<P, S>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                paddingLeft = 16.px
                paddingRight = 16.px
                width = LinearDimension.auto
                display = Display.flex
                flexDirection = FlexDirection.row
            }
            renderHeader()
        }
        styledDiv {
            css {
                marginTop = 16.px
                marginBottom = 16.px
            }
            mTable {
                mTableBody {
                    props.children?.invoke(PreferenceBuilder(this))
                }
            }
        }
    }

    protected open fun RBuilder.renderHeader() {
        props.title?.let {
            mTypography(variant = MTypographyVariant.h5) {
                +it
            }
        }
    }
}

actual inline fun PreferenceBuilder.preferenceCategory(
    crossinline handler: PreferenceCategoryProps.() -> Unit,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    base.child<PreferenceCategoryProps, PreferenceCategory<PreferenceCategoryProps, RState>> {
        attrs {
            handler()
            children = {
                it.builder()
            }
        }
    }
}

actual inline fun PreferenceBuilder.preferenceCategory(
    title: String,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    base.child<PreferenceCategoryProps, PreferenceCategory<PreferenceCategoryProps, RState>> {
        attrs {
            this.title = title
            children = {
                it.builder()
            }
        }
    }
}
