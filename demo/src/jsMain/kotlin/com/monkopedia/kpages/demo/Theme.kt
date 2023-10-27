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

import js.core.jso
import mui.material.PaletteMode
import mui.material.styles.ThemeOptions
import mui.material.styles.createTheme
import web.cssom.Color

private val themeOptions: ThemeOptions = jso {
    typography = jso { }
    typography?.useNextVariants = true
    typography?.button = jso {
        textTransform = "none"
    }
    // themeOptions.typography?.fontSize = 12
    palette = jso {
        mode = PaletteMode.light
        primary = jso {
            main = "#1a237e"
            light = "#534bae"
            dark = "#000051"
        }
        secondary = jso {
            main = "#009688"
            light = "#52c7b8"
            dark = "#00675b"
        }
        text = jso {
//            main = "#111111"
            primary = Color("#212121")
            secondary = Color("#616161")
        }
    }
}

val theme = createTheme(options = themeOptions)

private val invertedThemeOptions: ThemeOptions = jso {
    typography = jso()
    typography?.useNextVariants = true
    // themeOptions.typography?.fontSize = 12
    palette = jso {
        mode = PaletteMode.dark
        secondary = jso {
            main = "#1a237e"
            light = "#534bae"
            dark = "#000051"
        }
        primary = jso {
            main = "#009688"
            light = "#52c7b8"
            dark = "#00675b"
        }
        text = jso {
//            main = "#FAFAFA"
            primary = Color("#FAFAFA")
            secondary = Color("#EEEEEE")
        }
    }
}

val invertedTheme = createTheme(options = invertedThemeOptions)
