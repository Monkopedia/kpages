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
package com.monkopedia.asciifont

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.AbstractComponent
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.monkopedia.dynamiclayout.BaseDynamicLayout
import com.monkopedia.dynamiclayout.DynamicLayout
import com.monkopedia.dynamiclayout.HasDynamicLayout
import com.monkopedia.dynamiclayout.MeasureSpec
import com.monkopedia.dynamiclayout.MeasureSpec.Companion.unspecified
import com.monkopedia.lanterna.spannable.Spanned
import com.monkopedia.lanterna.spannable.StaticSpan
import com.monkopedia.lanterna.spannable.StyleSpan.Companion.withSpans

class AsciiLabel(font: Font, text: CharSequence) :
    AbstractComponent<AsciiLabel>(),
    HasDynamicLayout {
    override val dynamicLayout: DynamicLayout = AsciiLayout(this)
    var font: Font = font
        set(value) {
            field = value
            dynamicLayout.requestLayout()
        }
    var text: CharSequence = text
        set(value) {
            field = value
            dynamicLayout.requestLayout()
        }

    override fun createDefaultRenderer(): ComponentRenderer<AsciiLabel> {
        return AsciiRenderer()
    }
}

class AsciiLayout(override val component: AsciiLabel) : BaseDynamicLayout() {

    override fun measure(colSpec: MeasureSpec, rowSpec: MeasureSpec): TerminalSize {
        val desiredWidth = component.font.measureWidth(component.text)
        val desiredHeight = component.font.height
        return TerminalSize(
            colSpec.size(desiredWidth),
            rowSpec.size(desiredHeight)
        )
    }
}

class AsciiRenderer : ComponentRenderer<AsciiLabel> {
    override fun getPreferredSize(component: AsciiLabel): TerminalSize {
        return component.dynamicLayout.measure(
            unspecified(Int.MAX_VALUE),
            unspecified(Int.MAX_VALUE)
        )
    }

    override fun drawComponent(graphics: TextGUIGraphics, component: AsciiLabel) {
        val themeDefinition = component.themeDefinition
        graphics.applyThemeStyle(themeDefinition.normal)
        for (row in 0 until graphics.size.rows.coerceAtMost(component.font.height)) {
            when (val line = component.text) {
                is Spanned -> {
                    var index = 0
                    for (segment in line.toStaticSpans()) {
                        graphics.withSpans(segment.spans) {
                            index = graphics.putAscii(
                                index, row,
                                row,
                                component.font,
                                segment.toString().substring(0, segment.length)
                            )
                        }
                    }
                }
                is StaticSpan -> {
                    graphics.withSpans(line.spans) {
                        val end = (graphics.size.columns).coerceAtMost(line.length)
                        graphics.putAscii(
                            0, row,
                            row,
                            component.font,
                            line.toString().substring(0, end)
                        )
                    }
                }
                else -> {
                    val end = (graphics.size.columns).coerceAtMost(line.length)
                    graphics.putAscii(
                        0, row,
                        row,
                        component.font,
                        line.toString().substring(0, end)
                    )
                }
            }
        }
    }
}

private fun TextGUIGraphics.putAscii(col: Int, row: Int, fontRow: Int, font: Font, str: String):
    Int {
        var currentCol = col

        for (c in str) {
            if (currentCol > size.columns) {
                return currentCol
            }
            val line = font[c].rows[fontRow]
            putString(currentCol, row, line)
            currentCol += line.length
        }
        return currentCol
    }
