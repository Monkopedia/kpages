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
@file:OptIn(ExperimentalContracts::class)

package com.monkopedia.lanterna

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Border
import com.googlecode.lanterna.gui2.Borders
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.Separator
import com.googlecode.lanterna.gui2.Window
import com.monkopedia.asciifont.AsciiLabel
import com.monkopedia.asciifont.Font
import com.monkopedia.dynamiclayout.CachingPanel
import com.monkopedia.dynamiclayout.DynamicFrameLayout
import com.monkopedia.dynamiclayout.DynamicGridLayout
import com.monkopedia.dynamiclayout.DynamicHorizontalLinearLayout
import com.monkopedia.dynamiclayout.DynamicLayoutManager
import com.monkopedia.dynamiclayout.DynamicLinearLayout
import com.monkopedia.dynamiclayout.DynamicVerticalLinearLayout
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.Gravity
import com.monkopedia.dynamiclayout.GravityLayoutParams
import com.monkopedia.dynamiclayout.LayoutParams
import com.monkopedia.dynamiclayout.ScrollComponent
import com.monkopedia.dynamiclayout.SizeSpec
import com.monkopedia.dynamiclayout.WeightedLayoutParams
import com.monkopedia.lanterna.Lanterna.activeWindows
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.spannable.Spanned
import com.monkopedia.util.logger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

const val DEBUG_VIEWS = false
var logIndent = 0

val COMPONENT_LOGGER = ComponentHolder::class.logger

inline fun log(str: String) {
    if (DEBUG_VIEWS)
        COMPONENT_LOGGER.info("${Array(logIndent) { "  " }.joinToString("")}View: $str")
}

@DslMarker
annotation class LanternaUi

interface ComponentHolder {
    fun addComponent(component: Component)

    fun <T : Component> T.layoutParams(
        width: SizeSpec,
        height: SizeSpec
    ): T = also {
        it.layoutData = LayoutParams(width, height)
    }
}

object InvalidHolder : WeightedPanelHolder() {
    override fun addComponent(component: Component) {
        throw IllegalArgumentException("Children not supported")
    }
}

@LanternaUi
class WindowHolder(val window: CoroutineWindow) : ComponentHolder {
    private var hasComponent = false
    override fun addComponent(component: Component) {
        if (hasComponent) {
            throw IllegalStateException("Already contains component")
        }
        hasComponent = true
        window.component = component
    }
}

@LanternaUi
abstract class WeightedPanelHolder : ComponentHolder {

    inline fun <T : Component> T.layoutParams(
        width: SizeSpec,
        height: SizeSpec,
        gravity: Gravity = Gravity.TOP_LEFT,
        weight: Int = 0
    ): T = also {
        it.layoutData = WeightedLayoutParams(width, height, gravity, weight)
    }
}

@LanternaUi
class LinearPanelHolder<T : DynamicLayoutManager<*>>(val panel: Panel, val layout: T) :
    WeightedPanelHolder() {
    override fun addComponent(component: Component) {
        panel.addComponent(component)
    }
}

@LanternaUi
class FramePanelHolder(val panel: Panel, val layout: DynamicFrameLayout) : ComponentHolder {
    override fun addComponent(component: Component) {
        panel.addComponent(component)
    }

    inline fun <T : Component> T.layoutParams(
        width: SizeSpec,
        height: SizeSpec,
        gravity: Gravity = Gravity.TOP_LEFT
    ): T = also {
        it.layoutData = GravityLayoutParams(width, height, gravity)
    }
}

@LanternaUi
class ScrollHolder(val scroll: ScrollComponent) : ComponentHolder {
    override fun addComponent(component: Component) {
        scroll.component = component
    }
}

@LanternaUi
class BorderHolder(val border: Border) : ComponentHolder {
    private var hasComponent = false
    override fun addComponent(component: Component) {
        if (hasComponent) {
            throw IllegalStateException("Already contains component")
        }
        hasComponent = true
        border.component = component
    }
}

@LanternaUi
inline fun Screen.screenWindow(
    gui: MultiWindowTextGUI,
    centered: Boolean = false,
    init: WindowHolder.() -> Unit
): ScreenWindow {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return ScreenWindow(this).also {
        it.theme = Lanterna.gui.theme
        log("Open window")
        if (DEBUG_VIEWS) logIndent++
        if (centered) {
            it.setHints(listOf(Window.Hint.CENTERED))
        }
        WindowHolder(it).init()
        gui.addWindow(it)
        activeWindows.add(it)
        it.setEnableDirectionBasedMovements(false)
        if (DEBUG_VIEWS) logIndent--
        log("Close window")
    }
}

@LanternaUi
inline fun MultiWindowTextGUI.window(
    centered: Boolean = false,
    init: WindowHolder.() -> Unit
): ObservableWindow {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return ObservableWindow().also {
        it.theme = Lanterna.gui.theme
        log("Open window")
        if (DEBUG_VIEWS) logIndent++
        if (centered) {
            it.setHints(listOf(Window.Hint.CENTERED))
        }
        WindowHolder(it).init()
        addWindow(it)
        activeWindows.add(it)
        it.setEnableDirectionBasedMovements(false)
        if (DEBUG_VIEWS) logIndent--
        log("Close window")
    }
}

@LanternaUi
inline fun buildViews(init: ComponentHolder.() -> Unit): Collection<Component> {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    val components = mutableListOf<Component>()
    object : ComponentHolder {
        override fun addComponent(component: Component) {
            components.add(component)
        }
    }.init()

    return components
}

@LanternaUi
inline fun ComponentHolder.horizontal(
    init: LinearPanelHolder<DynamicHorizontalLinearLayout>.() -> Unit = {}
): CachingPanel {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return CachingPanel().also {
        log("Open horizontal")
        if (DEBUG_VIEWS) logIndent++
        val dynamicLinearLayout = DynamicLinearLayout(Direction.HORIZONTAL, it)
        it.layoutManager = dynamicLinearLayout
        LinearPanelHolder(it, dynamicLinearLayout as DynamicHorizontalLinearLayout).init()
        addComponent(it)
        if (DEBUG_VIEWS) logIndent--
        log("Close horizontal")
    }
}

@LanternaUi
inline fun CachingPanel.buildUi(init: LinearPanelHolder<DynamicLayoutManager<*>>.() -> Unit = {}) {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    val layout = layoutManager as DynamicLayoutManager<*>
    LinearPanelHolder(this, layout).init()
    layout.requestLayout()
}

@LanternaUi
inline fun ComponentHolder.vertical(
    init: LinearPanelHolder<DynamicVerticalLinearLayout>.() -> Unit
): CachingPanel {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return CachingPanel().also {
        log("Open vertical")
        if (DEBUG_VIEWS) logIndent++
        val dynamicLinearLayout = DynamicLinearLayout(Direction.VERTICAL, it)
        it.layoutManager = dynamicLinearLayout
        LinearPanelHolder(it, dynamicLinearLayout as DynamicVerticalLinearLayout).init()
        addComponent(it)
        if (DEBUG_VIEWS) logIndent--
        log("Close vertical")
    }
}

@LanternaUi
inline fun ComponentHolder.grid(
    cols: Int,
    init: LinearPanelHolder<DynamicGridLayout>.() -> Unit
): Panel {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return CachingPanel().also {
        log("Open grid")
        if (DEBUG_VIEWS) logIndent++
        val dynamicGridLayout = DynamicGridLayout(it, cols)
        it.layoutManager = dynamicGridLayout
        LinearPanelHolder(it, dynamicGridLayout).init()
        addComponent(it)
        if (DEBUG_VIEWS) logIndent--
        log("Close grid")
    }
}

@LanternaUi
inline fun ComponentHolder.frame(init: FramePanelHolder.() -> Unit): CachingPanel {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return CachingPanel().also {
        log("Open frame")
        if (DEBUG_VIEWS) logIndent++
        val dynamicFrameLayout = DynamicFrameLayout(it)
        it.layoutManager = dynamicFrameLayout
        FramePanelHolder(it, dynamicFrameLayout).init()
        addComponent(it)
        if (DEBUG_VIEWS) logIndent--
        log("Close frame")
    }
}

@LanternaUi
inline fun ComponentHolder.scroll(init: ScrollHolder.() -> Unit): ScrollComponent {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return ScrollComponent().also {
        log("Open vertical")
        if (DEBUG_VIEWS) logIndent++
        ScrollHolder(it).init()
        addComponent(it)
        if (DEBUG_VIEWS) logIndent--
        log("Close vertical")
    }
}

enum class BorderType(val factory: (String) -> Border) {
    SINGLE_LINE(Borders::singleLine),
    SINGLE_LINE_BEVEL(Borders::singleLineBevel),
    SINGLE_LINE_REVERSE_BEVEL(Borders::singleLineReverseBevel),
    DOUBLE_LINE(Borders::doubleLine),
    DOUBLE_LINE_BEVEL(Borders::doubleLineBevel),
    DOUBLE_LINE_REVERSE_BEVEL(Borders::doubleLineReverseBevel)
}

@LanternaUi
inline fun ComponentHolder.border(
    title: String = "",
    type: BorderType = BorderType.SINGLE_LINE,
    init: BorderHolder.() -> Unit
): Border {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return type.factory(title).also {
        log("Open border")
        if (DEBUG_VIEWS) logIndent++
        BorderHolder(it).init()
        addComponent(it)
        if (DEBUG_VIEWS) logIndent--
        log("Close border")
    }
}

@LanternaUi
fun ComponentHolder.button(text: String, action: Runnable = Runnable {}): Button {
    return Button(text, action).also {
        log("Button")
        addComponent(it)
    }
}

@LanternaUi
fun ComponentHolder.label(text: String): SpannableLabel {
    return SpannableLabel(text).also {
        log("Label")
        it.labelWidth = 0
        addComponent(it)
    }
}

@LanternaUi
fun ComponentHolder.asciiLabel(font: Font, text: CharSequence): AsciiLabel {
    return AsciiLabel(font, text).also {
        log("Ascii label")
        addComponent(it)
    }
}

@LanternaUi
fun ComponentHolder.hdiv(): Separator {
    return Separator(Direction.HORIZONTAL).also {
        it.layoutData = LayoutParams(Fill, SizeSpec.specify(1))
        // it.preferredSize = TerminalSize(1000, 1)
        log("hdiv")
        addComponent(it)
    }
}

@LanternaUi
fun ComponentHolder.vdiv(): Separator {
    return Separator(Direction.VERTICAL).also {
        it.layoutData = LayoutParams(SizeSpec.specify(1), Fill)
        log("vdiv")
        addComponent(it)
    }
}

@LanternaUi
fun ComponentHolder.space(size: Int): EmptySpace {
    return EmptySpace(TerminalSize(size, size)).also {
        log("space")
        addComponent(it)
    }
}

@LanternaUi
fun ComponentHolder.selectButton(text: String, onClick: () -> Unit): Selectable {
    lateinit var label: SpannableLabel
    border {
        label = label(text)
    }
    return object : Selectable {
        override fun onFire(navigation: Navigation): FocusResult {
            onClick()
            return ConsumeEvent
        }

        override var selected: Boolean = false
            set(value) {
                field = value
                label.setText(
                    Spanned().apply {
                        append(
                            text,
                            if (selected) EnableSGRSpan(SGR.REVERSE, SGR.BOLD)
                            else EnableSGRSpan(SGR.BOLD)
                        )
                    }
                )
            }
    }
}
