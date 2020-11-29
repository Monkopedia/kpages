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
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.mTypography
import com.ccfraser.muirwik.components.table.MTableCellPadding
import com.ccfraser.muirwik.components.table.mTableCell
import com.ccfraser.muirwik.components.table.mTableRow
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import kotlinx.css.Position
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.height
import kotlinx.css.left
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.width
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.css
import styled.styledDiv

actual external interface PreferenceBaseProps : RProps {
    actual var onClick: ((Any) -> Unit)?
}

actual external interface PreferenceProps : PreferenceBaseProps {
    actual var title: String?
    actual var subtitle: String?
}

abstract class PreferenceBase<P : PreferenceBaseProps, S : RState> : RComponent<P, S>() {
    override fun RBuilder.render() {
        mTableRow {
            mTableCell(padding = MTableCellPadding.none) {
                styledDiv {
                    css {
                        display = Display.flex
                        width = LinearDimension.auto
                        height = LinearDimension.maxContent
                        flexDirection = FlexDirection.column
                        position = Position.relative
                    }
                    styledDiv {
                        css {
                            padding(16.px)
                        }
                        renderPreference()
                    }
                    props.onClick?.let { listener ->
                        styledDiv {
                            css {
                                width = 100.pct
                                height = 100.pct
                                position = Position.absolute
                                left = 0.px
                            }
                            mButton(
                                "",
                                variant = MButtonVariant.text,
                                onClick = { listener(this@PreferenceBase) }
                            ) {
                                css {
                                    height = 100.pct
                                    width = 100.pct
                                }
                                styledDiv {
                                    css {
                                        asDynamic().all = "revert"
                                        alignItems = Align.start
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    abstract fun RBuilder.renderPreference()
}

class Preference : PreferenceBase<PreferenceProps, RState>() {
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
    }
}

actual inline fun PreferenceBuilder.preference(noinline handler: PreferenceProps.() -> Unit) {
    base.child(Preference::class) {
        attrs(handler)
    }
}
