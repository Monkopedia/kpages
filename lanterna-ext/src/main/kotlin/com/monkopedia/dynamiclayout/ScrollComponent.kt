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
package com.monkopedia.dynamiclayout

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.AbstractComposite
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.input.KeyType
import com.monkopedia.dynamiclayout.MeasureSpec.Companion.exactly
import com.monkopedia.dynamiclayout.MeasureSpec.Companion.unspecified
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.EventMatcher.Companion.matcher
import com.monkopedia.lanterna.Keymap
import com.monkopedia.lanterna.Lanterna.screen
import com.monkopedia.lanterna.on

fun interface ScrollListener {
    fun onScrollChanged(offset: Int)
}

class ScrollComponent : AbstractComposite<ScrollComponent>(), HasDynamicLayout {
    override val dynamicLayout: DynamicLayout = ScrollLayout(this)
    var offset: Int = 0
        set(value) {
            if (field == value) return
            field = value
            this.invalidate()
            scrollListeners.forEach {
                it.onScrollChanged(offset)
            }
        }

    val scrollListeners = mutableListOf<ScrollListener>()

    override fun toBasePane(position: TerminalPosition): TerminalPosition {
        return super.toBasePane(position.withRelativeRow(-offset))
    }

    override fun toGlobal(position: TerminalPosition): TerminalPosition {
        return super.toGlobal(position.withRelativeRow(-offset))
    }

    fun registerCommands(keymap: Keymap) {
        keymap.create("Scroll up") {
            offset--
            ConsumeEvent
        } on KeyType.ArrowUp.matcher()
        keymap.create("Scroll Down") {
            offset++
            ConsumeEvent
        } on KeyType.ArrowDown.matcher()
    }

    override fun setSize(size: TerminalSize?): ScrollComponent {
        return super.setSize(size).also {
            scrollListeners.forEach {
                it.onScrollChanged(offset)
            }
        }
    }

    override fun createDefaultRenderer(): ComponentRenderer<ScrollComponent> {
        var lastOffset = 0

        return object : ComponentRenderer<ScrollComponent> {
            override fun getPreferredSize(component: ScrollComponent): TerminalSize {
                return component.component.preferredSize.max(screen.terminalSize)
            }

            override fun drawComponent(graphics: TextGUIGraphics, component: ScrollComponent) {
                val child = component.component
                val maxPosition = child.position.row + child.size.rows - graphics.size.rows
                if (offset > maxPosition) {
                    offset = maxPosition
                }
                if (offset < 0) {
                    offset = 0
                }
                if (offset != lastOffset) {
                    val position = toGlobal(TerminalPosition.TOP_LEFT_CORNER)
                    graphics.textGUI.screen.scrollLines(
                        0 - position.column,
                        graphics.size.columns - position.column,
                        offset - lastOffset
                    )
                    lastOffset = offset
                }
                val componentGraphics =
                    graphics.newTextGraphics(child.position.withRelativeRow(-offset), child.size)
                child.draw(componentGraphics)
            }
        }
    }

    class ScrollLayout(override val component: ScrollComponent) : BaseDynamicLayout() {
        private val child get() = component.component.asDynamicLayout
        private var lastMeasure: List<Any>? = null

        override fun measure(cols: MeasureSpec, rows: MeasureSpec): TerminalSize {
            val childRows = unspecified(0)
            val childSize = child.measure(cols, childRows)
            return TerminalSize(
                cols.size(childSize.columns),
                rows.size(childSize.rows)
            ).also {
                lastMeasure = listOf(cols, rows, it)
            }
        }

        override fun layout(area: TerminalSize, position: TerminalPosition) {
            super.layout(area, position)
            val childCache = child
            val childSize = childCache.measure(exactly(area.columns), unspecified(0))
            childCache.layout(childSize, TerminalPosition.TOP_LEFT_CORNER)
        }
    }
}
