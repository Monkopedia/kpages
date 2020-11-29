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
package com.monkopedia.lanterna

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType.Backspace
import com.googlecode.lanterna.input.KeyType.Character
import com.googlecode.lanterna.input.KeyType.Enter
import com.monkopedia.dynamiclayout.CachingPanel
import com.monkopedia.dynamiclayout.DynamicFrameLayout
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.lanterna.Lanterna.gui
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.spannable.StaticSpan
import com.monkopedia.lanterna.spannable.ThemeSpan

class TextInput : CachingPanel(), Focusable {
    private val layout = DynamicFrameLayout(this)
    private var layoutBuilder = LinearPanelHolder(this, layout)
    private var selectionManager: SelectionManager? = null

    val asSelectable by lazy {
        object : Selectable {
            override fun onFire(navigation: Navigation): FocusResult {
                return onInput(KeyStroke(Enter))
            }

            override var selected: Boolean = false
                set(value) {
                    field = value
                    if (value) {
                        takeFocus()
                    }
                }

            override fun setSelectionManager(manager: SelectionManager) {
                selectionManager = manager
            }
        }
    }

    var onFireListener: (() -> FocusResult)? = null
    var onTextChangedListener: (() -> Unit)? = null
    val label = buildViews { label("").layoutParams(Fill, Wrap) }.first() as SpannableLabel

    var text: CharSequence? = null
        set(value) {
            field = value
            updateText()
            invalidate()
        }
    var hint: CharSequence? = null
        set(value) {
            field = value
            updateText()
        }

    override var focused: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private fun updateText() {
        val currentText = text
        if (currentText.isNullOrEmpty()) {
            label.setText(
                StaticSpan(
                    " $hint",
                    listOf(ThemeSpan(gui.theme.defaultDefinition.insensitive))
                )
            )
        } else {
            label.setText(currentText)
        }
        onTextChangedListener?.invoke()
    }

    init {
        layoutManager = layout
        layoutBuilder.apply {
            border {
                addComponent(label)
                label
            }.layoutParams(Fill, Wrap)
        }
    }

    override fun onInput(keyStroke: KeyStroke): FocusResult {
        return when (keyStroke.keyType) {
            Character -> ConsumeEvent.also {
                text = "${text ?: ""}${keyStroke.character}"
            }
            Backspace -> ConsumeEvent.also {
                text = text?.subSequence(0, ((text?.length ?: 1) - 1).coerceAtLeast(0))
            }
            Enter -> onFireListener?.invoke() ?: Unhandled
            else -> selectionManager?.inputFromSelectable(keyStroke) ?: Unhandled
        }
    }

    override fun createDefaultRenderer(): ComponentRenderer<Panel> {
        return object : ComponentRenderer<Panel> {
            override fun getPreferredSize(panel: Panel): TerminalSize {
                return panel.layoutManager.getPreferredSize(panel.childrenList)
            }

            override fun drawComponent(graphics: TextGUIGraphics, panel: Panel) {
                panel as TextInput
                val components = panel.childrenList
                if (panel.isInvalid) {
                    panel.layoutManager.doLayout(graphics.size, components)
                }

                for (child in components) {
                    val componentGraphics =
                        graphics.newTextGraphics(child.position, child.size)
                    child.draw(componentGraphics)
                }
                if (panel.focused) {
                    graphics.setCharacter((text?.length ?: 0) + 1, 1, Symbols.SINGLE_LINE_VERTICAL)
                } else {
                    graphics.setCharacter((text?.length ?: 0) + 1, 1, ' ')
                }
            }
        }
    }
}

private operator fun TerminalPosition.compareTo(size: TerminalSize): Int {
    return when {
        column > size.columns -> 1
        row > size.rows -> 1
        row == size.rows -> size.columns - column
        else -> size.rows - row
    }
}
