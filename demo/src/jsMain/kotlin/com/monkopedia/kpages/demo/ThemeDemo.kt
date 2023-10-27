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

import com.monkopedia.kpages.ViewControllerFactory
import com.monkopedia.kpages.factory
import emotion.css.css
import emotion.react.css
import js.core.jso
import mui.icons.material.Add
import mui.icons.material.CloudUpload
import mui.icons.material.ContentCopy
import mui.icons.material.MoreVert
import mui.icons.material.Print
import mui.icons.material.Search
import mui.icons.material.Share
import mui.material.AppBar
import mui.material.AppBarColor
import mui.material.AppBarPosition
import mui.material.Button
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.material.Card
import mui.material.CardActions
import mui.material.CardHeader
import mui.material.Checkbox
import mui.material.CheckboxColor
import mui.material.Fab
import mui.material.FabColor
import mui.material.FormControlMargin
import mui.material.IconButton
import mui.material.IconButtonColor
import mui.material.IconButtonEdge
import mui.material.ListItemIcon
import mui.material.Menu
import mui.material.MenuItem
import mui.material.MenuList
import mui.material.Radio
import mui.material.RadioColor
import mui.material.Size
import mui.material.Snackbar
import mui.material.Switch
import mui.material.SwitchColor
import mui.material.TextField
import mui.material.TextFieldColor
import mui.material.Toolbar
import mui.material.ToolbarVariant
import mui.material.Typography
import mui.material.styles.Theme
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.ChildrenBuilder
import react.FC
import react.Props
import react.create
import react.dom.div
import react.dom.html.HTMLAttributes
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.path
import react.dom.svg.ReactSVG.rect
import react.dom.svg.ReactSVG.svg
import react.useState
import web.cssom.AlignContent
import web.cssom.AlignItems
import web.cssom.Auto
import web.cssom.BoxSizing
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Display
import web.cssom.Flex
import web.cssom.FlexDirection
import web.cssom.Length
import web.cssom.Margin
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.PropertyName.Companion.boxSizing
import web.cssom.PropertyName.Companion.display
import web.cssom.PropertyName.Companion.fontFamily
import web.cssom.pct
import web.cssom.px
import web.cssom.string
import web.cssom.translate
import web.cssom.translatey
import web.html.HTMLDivElement

actual val ThemeDemoFactory: ViewControllerFactory = factory {
    ThemeDemo()
}

val MaterialSvg = FC<Props> {
    svg {
        x = 0.0
        y = 0.0
        viewBox = "0 0 300 300"
        g {
            rect {
                className = ClassName("material-image__background")
                x = 0.3
                y = 0.5
                width = 300.0
                height = 300.0
                fill = theme.palette.secondary.dark.toString()
            }
            g {
                path {
                    className = ClassName("material-image--shape-circle")
                    fill = theme.palette.secondary.light.toString()
                    d = "M124.5,171l26.3-45.5c-7.7-4.6-16.7-7.2-26.3-7.2c-28.6,0-51.7,23.2" +
                        "-51.7,51.7c0,28.6,23.2,51.7,51.7,51.7 c28.3,0,51.2-22.6,51.7-50.8H124.5z"
                }
                path {
                    className = ClassName("material-image--shape-triangle")
                    fill = theme.palette.secondary.light.toString()
                    d = "M176.2,170.1c0,0.3,0,0.6,0,1H228l-51.7-89.6l-25.5,44.1" +
                        "C166,134.5,176.2,151.1,176.2,170.1z"
                }
                path {
                    className = ClassName("material-image--shape-intersection")
                    fill = theme.palette.secondary.dark.toString()
                    d = "M176.2,171c0-0.3,0-0.6,0-1c0-19-10.2-35.6-25.5-44.6L124.5,171H176.2z"
                }
            }
        }
    }
}

private object ComponentStyles {

    val flex = emotion.css.ClassName {
        flex = "flex-grow".unsafeCast<Flex>()
        boxSizing = BoxSizing.borderBox
    }

    val column = emotion.css.ClassName {
        display = Display.flex
        flexDirection = FlexDirection.column
    }
    val row = emotion.css.ClassName {
        display = Display.flex
        flexDirection = FlexDirection.row
    }
    val alignCenter = emotion.css.ClassName {
        alignItems = AlignItems.center
    }

    val sectionColumn = emotion.css.ClassName(column, alignCenter) {
        marginLeft = 16.px
        marginRight = 16.px
    }
    val mainArea = emotion.css.ClassName {
        display = Display.flex
        alignItems = AlignItems.center
        boxSizing = BoxSizing.borderBox
        fontFamily = string("Roboto")
        margin = Margin(64.px, Auto.auto)
        width = 1664.px
        this.media("all and (max-width: 880px)") {
            flexDirection = FlexDirection.column
        }
        this.media("all and (max-width: 1712px)") {
            margin = Margin(32.px, Auto.auto)
            width = 832.px
        }
    }
    val columnHolder = emotion.css.ClassName {
        display = Display.flex
        this.media("all and (max-width: 1712px)") {
            flexDirection = FlexDirection.column
        }
    }

    val toolbar = emotion.css.ClassName(column) {
        backgroundColor = Color("#EEEEEE")
        width = 400.px
        height = 620.px
        padding = 30.px
    }

    val smallSection = emotion.css.ClassName(column, alignCenter) {
        backgroundColor = Color("#EEEEEE")
        width = 400.px
        height = 132.px
        alignContent = AlignContent.center
        alignItems = AlignItems.center
        display = Display.flex
        padding = 16.px
    }

    val largeSection = emotion.css.ClassName(column, alignCenter) {
        backgroundColor = Color("#EEEEEE")
        width = 400.px
        height = 400.px
        alignContent = AlignContent.center
        alignItems = AlignItems.center
        display = Display.flex
        padding = 16.px
    }
    val largeRowSection = emotion.css.ClassName(row, alignCenter) {
        backgroundColor = Color("#EEEEEE")
        width = 400.px
        height = 400.px
    }
    val card = emotion.css.ClassName {
        borderRadius = 2.px
        width = 262.px
    }
}

val ThemeDemo = FC<Props> {
    div {
        className = ComponentStyles.mainArea
        div {
            className = ComponentStyles.columnHolder
            div {
                className = ComponentStyles.sectionColumn
                buttons(theme)
                span { className = (ComponentStyles.flex) }
                selection(theme)
            }
            div {
                className = ComponentStyles.sectionColumn
                switchesAndSlides()
                span { className = (ComponentStyles.flex) }
                cards(theme)
            }
            div {
                className = ComponentStyles.sectionColumn
                menu(theme)
                span { className = ComponentStyles.flex }
                textFields(theme)
            }
            div {
                className = ComponentStyles.sectionColumn
                toolbar(theme)
            }
        }
    }
}

private fun ChildrenBuilder.toolbar(theme: Theme) {
    Typography {
        +"Tool Bar"
        variant = TypographyVariant.subtitle2
        sx { color = Color("text-secondary") }
    }
    div {
        className = ComponentStyles.toolbar
        column {
            css {
                width = 340.px
                height = 560.px
            }

            div {
                css {
                    height = 24.px
                    width = 100.pct
                    backgroundColor = theme.palette.primary.dark
                }
            }
            AppBar {
                color = AppBarColor.primary
                position = AppBarPosition.static
                Toolbar {
                    variant = ToolbarVariant.dense
                    IconButton {
                        Menu()
                        edge = IconButtonEdge.start
                        color = IconButtonColor.inherit
                    }
                    +"Title"
//                    ToolbarTitle("Title")
                    IconButton {
                        Search()
                        edge = IconButtonEdge.end
                        color = IconButtonColor.inherit
                    }
                    IconButton {
                        MoreVert()
                        color = IconButtonColor.inherit
                        edge = IconButtonEdge.end
                    }
                }
            }
            div {
                css {
                    height = 220.px
                    overflow = Overflow.hidden
                }
                div {
                    css {
                        transform = translatey((-64).px)
                    }
                    className = ClassName("material-image")
                    MaterialSvg()
                }
            }
            div {
                css {
                    backgroundColor =
                        Color(theme.palette.background.paper)
                    width = 100.pct
                    paddingLeft = 24.px
                    paddingRight = 32.px
                    flex = "flex-grow".unsafeCast<Flex>()
                }
                div {
                    css {
                        marginLeft = Auto.auto
                        width = 48.px
                        transform = translate(8.px, (-24).px)
                    }
                    Fab {
                        Add()
                        color = FabColor.secondary
                    }
                }
                Typography {
                    +(
                        "Pellentesque habitant morbi tristique senectus et netus et " +
                            "malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat " +
                            "vitae, ultricies eget, tempor sit amet, ante. Donec eu libero sit " +
                            "amet quam egestas semper. Aenean ultricies mi vitae est. " +
                            "Pellentesque habitant morbi tristique senectus et netus et " +
                            "malesuada fames ac turpis egestas. Vestibulum tortor quam, " +
                            "feugiat vitae, ultricies eget, tempor sit amet, ante"
                        )
                    variant = TypographyVariant.caption
                }
            }
        }
    }
}

private fun ChildrenBuilder.menu(theme: Theme) {
    Typography {
        variant = TypographyVariant.subtitle2
        +"Menu"
    }
    div {
        className = ComponentStyles.largeSection
        div {
            css {
                position = Position.absolute
                width = 368.px
            }
            AppBar {
                color = AppBarColor.secondary
                position = AppBarPosition.static
                Toolbar {
                    variant = ToolbarVariant.dense
                    +"Title"
//                    ToolbarTitle("Title")
                }
            }
        }
        // mDrawer(true) {
        div {
            css {
                backgroundColor = Color("#0000004d")
                position = Position.absolute
                width = 368.px
                height = 368.px
                display = Display.flex
            }
            MenuList {
                css {
                    backgroundColor =
                        Color(theme.palette.background.paper)
                    width = 240.px
                }
                div {
                    css {
                        height =
                            140.px
                        width = 100.pct
                        overflow = Overflow.hidden
                        transform = translatey((-8).px)
                    }
                    div {
                        css {
                            transform = translatey((-56).px)
                        }
                        className = ClassName("material-image")
                        svg {
                            x = 0.0
                            y = 0.0
                            viewBox = "0 0 300 300"
//                                        style = "enable-background:new 0 0 300 300;"
                            g {
                                rect {
                                    x = 0.3
                                    y = 0.5
                                    width = 300.0
                                    height = 300.0
                                    className = ClassName("material-image__background")
                                }
                                g {
                                    path {
                                        className = ClassName("material-image--shape-circle")
                                        fill = "${theme.palette.primary.light}"
                                        d = "M124.5,171l26.3-45.5c-7.7-4.6-16.7-7.2-26.3-7.2" +
                                            "c-28.6,0-51.7,23.2-51.7,51.7" +
                                            "c0,28.6,23.2,51.7,51.7,51.7 " +
                                            "c28.3,0,51.2-22.6,51.7-50.8H124.5z"
                                    }
                                    path {
                                        className = ClassName("material-image--shape-triangle")
                                        fill = "${theme.palette.primary.light}"
                                        d = "M176.2,170.1c0,0.3,0,0.6,0,1H228l-51.7-89.6" +
                                            "l-25.5,44.1C166,134.5,176.2,151.1,176.2,170.1z"
                                    }
                                    path {
                                        className = ClassName("material-image--shape-intersection")
                                        d = "M176.2,171c0-0.3,0-0.6,0-1c0-19-10.2-35.6-25.5-44.6" +
                                            "L124.5,171H176.2z"
                                    }
                                }
                            }
                        }
                    }
                }
                div {
                    css {
                        height = (-8).px
                    }
                }
                MenuItem {
//                    button = true
                    ListItemIcon { Share() }
                    Typography {
                        +"Share"
//                        variant = TypographyVariant.inherit
                    }
                }
                MenuItem {
//                    button = true
                    selected = true
                    ListItemIcon { CloudUpload() }
                    Typography {
                        +"Upload"
//                        variant = TypographyVariant.inherit
                    }
                }
                MenuItem {
//                    button = true
                    ListItemIcon { ContentCopy() }
                    Typography {
                        +"Copy"
//                        variant = TypographyVariant.inherit
                    }
                }
                MenuItem {
//                    button = true
                    ListItemIcon { Print() }
                    Typography {
                        +"Print this page"
//                        variant = TypographyVariant.inherit
                    }
                }
            }
        }
    }
}

private fun ChildrenBuilder.textFields(theme: Theme) {
    Typography {
        +"Text Field"
        variant = TypographyVariant.subtitle2
        sx { color = Color("text-secondary") }
    }
    column {
        className = ComponentStyles.smallSection
        row {
            css {
                width = 100.pct
                height = 50.pct
            }
            Typography {
                +"Primary"
                variant = TypographyVariant.body2
                sx { color = Color("text-primary") }
                css {
                    paddingTop = 22.px
                    width = 25.pct
                }
            }
            TextField {
                +"Single-line input label"
                defaultValue = "Input text for a single line"
                placeholder = "Single-line-input label"
                margin = FormControlMargin.none
                color = TextFieldColor.primary
            }
        }
        row {
            css {
                width = 100.pct
                height = 50.pct
            }

            Typography {
                +"Secondary"
                variant = TypographyVariant.body2
                sx { color = Color("text-primary") }
                css {
                    paddingTop = 22.px
                    width = 25.pct
                }
            }
            TextField {
                "Single-line input label"
                defaultValue = "Input text for a single line"
                placeholder = "Single-line-input label"
                margin = FormControlMargin.none
                color = TextFieldColor.secondary
            }
        }
    }
}

private fun ChildrenBuilder.cards(theme: Theme) {
    Typography {
        +"Cards"
        variant = TypographyVariant.subtitle2
        sx { color = Color("text-secondary") }
    }
    div {
        className = ComponentStyles.largeSection
        Card {
            raised = true
            classes = jso {
                root = ComponentStyles.card
            }
            CardHeader {
                title = Typography.create {
                    +"Title"
                }
                subheader = Typography.create {
                    +"Subhead"
                }
                avatar = div.create {
                    css {
                        backgroundColor =
                            theme.palette.text.secondary
                        borderRadius = 20.px
                        width = 40.px
                        height = 40.px
                    }
                }
            }
            div {
                css {
                    height = 140.px
                    overflow = Overflow.hidden
                }
                div {
                    css {
                        transform = translatey((-64).px)
                    }
                    className = ClassName("material-image")
                    MaterialSvg()
                }
            }
            Typography {
                +(
                    "Pellentesque habitant morbi tristique senectus et netus et malesuada " +
                        "fames ac turpis egestas."
                    )
                variant = TypographyVariant.body2
                css {
                    padding = (16.px)
                }
            }
            CardActions {
                disableSpacing = true
                div {
                    css {
                        marginLeft = Auto.auto
                        paddingRight = 8.px
                        paddingBottom = 8.px
                    }

                    Button {
                        +"Button"
                        variant = ButtonVariant.contained
                        color = ButtonColor.secondary
                        size = Size.small
                    }
                }
            }
        }
    }
}

val switchesAndSlides = FC<Props> { props ->
    var firstChecked: Boolean? by useState()
    var secondChecked: Boolean? by useState()
    var firstSelected: Boolean? by useState()
    var firstSwitched: Boolean? by useState()
    var secondSwitched: Boolean? by useState()
    Typography {
        +"Switches and sliders"
        variant = TypographyVariant.subtitle2
        sx { color = Color("text-secondary") }
    }
    column {
        className = ComponentStyles.smallSection
        row {
            css {
                width = 100.pct
                height = 50.pct
                alignContent = AlignContent.center
                alignItems = AlignItems.center
            }
            Typography {
                +"Primary"
                variant = TypographyVariant.body2
                sx { color = Color("text-primary") }
                css {
                    paddingLeft = 16.px
                    width = 200.px
                }
            }
            Checkbox {
                checked = firstChecked ?: false
                color = CheckboxColor.primary
                onChange = { _, selected ->
                    firstChecked = selected
                }
            }

            Radio {
                checked = firstSelected ?: false
                color = RadioColor.primary
                onChange = { _, selected ->
                    firstSelected = true
                }
            }
            Switch {
                checked = firstSwitched ?: false
                color = SwitchColor.primary
                onChange = { _, selected ->
                    firstSwitched = selected
                }
            }
        }
        row {
            css {
                width = 100.pct
                height = 50.pct
                alignContent = AlignContent.center
                alignItems = AlignItems.center
            }
            Typography {
                +"Secondary"
                variant = TypographyVariant.body2
                sx { color = Color("text-primary") }
                css {
                    paddingLeft = 16.px
                    width = 200.px
                }
            }
            Checkbox {
                checked = secondChecked ?: false
                color = CheckboxColor.secondary
                onChange = { _, selected ->
                    secondChecked = selected
                }
            }

            Radio {
                checked = !(firstSelected ?: false)
                color = RadioColor.secondary
                onChange = { _, selected ->
                    firstSelected = false
                }
            }
            Switch {
                checked = secondSwitched ?: false
                color = SwitchColor.secondary
                onChange = { _, selected ->
                    secondSwitched = selected
                }
            }
        }
    }
}

private fun ChildrenBuilder.selection(theme: Theme) {
    Typography {
        +"Selection"
        variant = TypographyVariant.subtitle2
        sx { color = Color("text-secondary") }
    }
    column {
        className = ComponentStyles.smallSection
        row {
            css {
                height = 50.pct
            }
            Typography {
                +"Primary"
                variant = TypographyVariant.body2
                sx { color = Color("text-primary") }
                css {
                    paddingLeft = 16.px
                    paddingBottom = 40.px
                }
            }
            Snackbar {
                content = "Single-line snackbar"
                open = true
                autoHideDuration = null
                action = div.create {
                    Button {
                        +"Action"
                        color = ButtonColor.primary
                        variant = ButtonVariant.text
                        size = Size.small
                    }
                }
            }
        }
        row {
            css {
                height = 50.pct
            }
            Typography {
                +"Secondary"
                variant = TypographyVariant.body2
                sx { color = Color("text-primary") }
                css {
                    paddingLeft = 16.px
                    paddingBottom = 52.px
                }
            }
            Snackbar {
                content = "Single-line snackbar"
                open = true
                autoHideDuration = null
                action = div.create {
                    Button {
                        +"Action"
                        color = ButtonColor.secondary
                        variant = ButtonVariant.text
                        size = Size.small
                    }
                }
            }
        }
    }
}

private fun ChildrenBuilder.buttons(theme: Theme) {
    Typography {
        +"Buttons"
        variant = TypographyVariant.subtitle2
        sx { color = Color("text-secondary") }
    }
    div {
        className = ComponentStyles.largeRowSection
        column {
            className = ComponentStyles.alignCenter
            css {
                width = 50.pct
            }

            Typography {
                +"Primary"
                variant = TypographyVariant.body2
                sx { color = Color("text-primary") }
            }
            Fab {
                Add()
                color = FabColor.primary
                size = Size.large
            }
            space(32.px)

            Fab {
                Add()
                size = Size.large
                css {
                    this.backgroundColor =
                        Color(theme.palette.background.default)
                    this.color = theme.palette.primary.main
                }
            }
            space(32.px)
            Button {
                +"Normal"
                color = ButtonColor.primary
                variant = ButtonVariant.text
            }
            space(32.px)
            Button {
                +"Normal"
                color = ButtonColor.primary
                variant = ButtonVariant.contained
            }
        }
        column {
            className = ComponentStyles.alignCenter
            css {
                width = 50.pct
            }

            Typography {
                +"Secondary"
                variant = TypographyVariant.body2
                sx { color = Color("text-primary") }
            }
            Fab {
                Add()
                color = FabColor.secondary
                size = Size.large
            }
            div {
                css {
                    height = 32.px
                }
            }

            Fab {
                Add()
                size = Size.large
                css {
                    this.backgroundColor =
                        Color(theme.palette.background.default)
                    this.color =
                        theme.palette.secondary.main
                }
            }
            div {
                css {
                    height = 32.px
                }
            }
            Button {
                +"Normal"
                color = ButtonColor.secondary
                variant = ButtonVariant.text
            }
            div {
                css {
                    height = 32.px
                }
            }
            Button {
                +"Normal"
                color = ButtonColor.secondary
                variant = ButtonVariant.contained
            }
        }
    }
}

private inline fun ChildrenBuilder.column(
    crossinline content: HTMLAttributes<HTMLDivElement>.() -> Unit
) {
    div {
        className = ComponentStyles.column
        content()
    }
}

private inline fun ChildrenBuilder.row(
    crossinline content: HTMLAttributes<HTMLDivElement>.() -> Unit
) {
    div {
        className = ComponentStyles.row
        content()
    }
}

private fun ChildrenBuilder.flex() {
}

private fun ChildrenBuilder.space(s: Length) {
    div {
        css {
            width = s
            height = s
        }
    }
}
