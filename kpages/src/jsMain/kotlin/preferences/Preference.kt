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
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.TableCell
import mui.material.TableCellPadding
import mui.material.TableRow
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import web.cssom.AlignContent
import web.cssom.AlignItems
import web.cssom.Auto
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.Length
import web.cssom.Position
import web.cssom.pct
import web.cssom.px

external interface PreferencePropsHolder<T : PreferenceBaseProps> : PropsWithChildren {
    var preferenceProps: T
}

actual external interface PreferenceBaseProps {
    actual var onClick: (suspend (Any) -> Unit)?
}

actual external interface PreferenceProps : PreferenceBaseProps {
    actual var title: String?
    actual var subtitle: String?
}

val PreferenceBase = FC<PreferencePropsHolder<PreferenceBaseProps>> { props ->
    TableRow {
        TableCell {
            padding = TableCellPadding.none
            div {
                css {
                    display = Display.flex
                    width = Auto.auto
                    height = Length.maxContent
                    flexDirection = FlexDirection.column
                    position = Position.relative
                }
                div {
                    css {
                        padding = 16.px
                    }
                    +props.children
                }
                props.preferenceProps.onClick?.let { listener ->
                    div {
                        css {
                            width = 100.pct
                            height = 100.pct
                            position = Position.absolute
                            left = 0.px
                        }
                        Button {
                            variant = ButtonVariant.text
                            onClick = {
                                GlobalScope.launch {
                                    listener(this@FC)
                                }
                            }
                            css {
                                height = 100.pct
                                width = 100.pct
                            }
                            div {
                                css {
                                    asDynamic().all = "revert"
                                    alignItems = AlignItems.start
                                    alignContent = AlignContent.center
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val Preference = FC<PreferencePropsHolder<PreferenceProps>> { props ->
    PreferenceBase {
        +props
        PreferenceText {
            +props
        }
    }
}

val PreferenceText = FC<PreferencePropsHolder<PreferenceProps>> { props ->
    props.preferenceProps.title?.let {
        Typography {
            variant = TypographyVariant.subtitle1
            sx { color = Color("text-primary") }
            +it
        }
    }
    props.preferenceProps.subtitle?.let {
        Typography {
            variant = TypographyVariant.body2
            sx { color = Color("text-secondary") }
            +it
        }
    }
}

actual inline fun PreferenceBuilder.preference(noinline handler: PreferenceProps.() -> Unit) {
    base.apply {
        Preference {
            preferenceProps = jso {
                handler()
            }
        }
    }
}
