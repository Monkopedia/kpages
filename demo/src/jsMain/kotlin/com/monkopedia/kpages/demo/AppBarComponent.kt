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
package com.monkopedia.kpages.demo

import emotion.react.css
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mui.icons.material.Close
import mui.icons.material.Edit
import mui.icons.material.Save
import mui.icons.material.Search
import mui.material.AppBar
import mui.material.CircularProgress
import mui.material.CircularProgressColor
import mui.material.FormControlMargin
import mui.material.FormControlVariant
import mui.material.IconButton
import mui.material.IconButtonColor
import mui.material.IconButtonEdge
import mui.material.TextField
import mui.material.Toolbar
import mui.material.Typography
import mui.material.styles.ThemeProvider
import org.w3c.dom.url.URLSearchParams
import react.FC
import react.Props
import react.State
import react.create
import react.dom.onChange
import react.useState
import styled.StyleSheet
import web.cssom.Flex
import web.cssom.px

external interface AppBarState : State

external interface AppBarProps : Props {
    var onSearchChanged: ((String?) -> Unit)?
}

private object AppBarComponentStyles : StyleSheet("AppBar", isStatic = true) {
    val textField by css {
    }

    val appBar by css {
    }
}

val AppBarComponent = FC<AppBarProps> { props ->
    var search: String? by useState(null)
    var showingSearch: Boolean by useState(false)
    var saving: Boolean by useState(false)

    fun startSave() {
        saving = true
        GlobalScope.launch {
            saving = false
            val searchParams = URLSearchParams(window.location.search)
            searchParams.set("edit", "false")
            window.location.search = searchParams.toString()
        }
    }
    AppBar {
        css {
            height = 64.px
        }
        Toolbar {
            if (showingSearch == true) {
                ThemeProvider {
                    theme = invertedTheme
                    TextField {
                        label = Typography.create {
                            +"Search"
                        }
                        variant = FormControlVariant.outlined
                        margin = FormControlMargin.dense
                        onChange = { e ->
                            props.onSearchChanged?.invoke(e.asDynamic().targetInputValue)
                        }
                        css {
                            height = 48.px
                            marginRight = 32.px
                            flex = 1.0.unsafeCast<Flex>()
                        }
                        this.autoFocus = true
                        this.onKeyUp = {
                            if (it.asDynamic().key == "Escape") {
                                showingSearch = false
                            }
                        }
                    }
                }
                IconButton {
                    Close()
                    edge = IconButtonEdge.end
                    color = IconButtonColor.inherit
                    onClick = {
                        showingSearch = false
                    }
                }
            } else {
                props.onSearchChanged?.invoke(null)
                +"iMDex"
                val searchParams = URLSearchParams(window.location.search)
                val isEditing = searchParams.get("edit")?.toBoolean() ?: false
                if (saving == true) {
                    CircularProgress {
                        color = CircularProgressColor.inherit
                        size = 24.px
                    }
                } else {
                    IconButton {
                        if (isEditing) Save() else Edit()
                        edge = IconButtonEdge.end
                        color = IconButtonColor.inherit
                        onClick = {
                            if (isEditing) {
                                startSave()
                            } else {
                                searchParams.set("edit", "${!isEditing}")
                                window.location.search = searchParams.toString()
                            }
                        }
                    }
                }
                IconButton {
                    Search()
                    edge = IconButtonEdge.end
                    color = IconButtonColor.inherit
                    onClick = {
                        showingSearch = true
                    }
                }
            }
        }
    }
}
