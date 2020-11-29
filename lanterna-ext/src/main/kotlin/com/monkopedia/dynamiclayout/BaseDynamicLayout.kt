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
import com.googlecode.lanterna.gui2.Border
import com.googlecode.lanterna.gui2.Component

abstract class BaseDynamicLayout : DynamicLayout {
    private var lastMeasure: List<Any>? = null

    override fun measure(colSpec: MeasureSpec, rowSpec: MeasureSpec): TerminalSize {
        val size = component.preferredSize
        val cols = colSpec.size(size.columns)
        val rows = rowSpec.size(size.rows)
        return TerminalSize(cols, rows).also {
            lastMeasure = listOf(colSpec, rowSpec, it)
        }
    }

    override fun layout(size: TerminalSize, pos: TerminalPosition) {
        component.size = size
        component.position = pos
    }

    override val isCompatLayout: Boolean
        get() = false

    override fun requestLayout() {
        component.parent?.asDynamicLayout?.requestLayout()
            ?: component.invalidate()
    }

    override fun debugString(): String {
        return "${this::class.simpleName}: ${component::class.simpleName} " +
            "${component.position} ${component.size}"
    }
}

class CompatDynamicLayout(override val component: Component) : BaseDynamicLayout() {
    override val isCompatLayout: Boolean
        get() = true
}

class BorderLayout(override val component: Border) : BaseDynamicLayout() {
    private var lastMeasure: List<Any>? = null
    val childLayout get() = component.component.asDynamicLayout

    override fun layout(size: TerminalSize, pos: TerminalPosition) {
        super.layout(size, pos)
        childLayout.layout(size.withRelative(-2, -2), TerminalPosition.OFFSET_1x1)
    }

    override fun measure(cols: MeasureSpec, rows: MeasureSpec): TerminalSize {
        return childLayout.measure(cols - 2, rows - 2).withRelative(2, 2).also {
            lastMeasure = listOf(cols, rows, it)
        }
    }

    override fun debugString(): String {
        return "BorderLayout:  ${component.position} ${component.size}"
    }
}
