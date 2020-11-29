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
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.LayoutManager
import com.googlecode.lanterna.gui2.Panel
import com.monkopedia.lanterna.Lanterna.screen

abstract class DynamicLayoutManager<T : LayoutParams>(
    private val companion: LayoutParams.LayoutCompanion<T>,
    override val component: Panel
) : BaseDynamicLayout(), LayoutManager {
    private var needsLayout = true
        set(value) {
            if (field == value) return
            field = value
            if (value) {
                doRequestLayout()
            }
        }
    private var cachedPreferredSize: TerminalSize? = null

    private var lastArea: TerminalSize = TerminalSize.ZERO
    private var components = listOf<Component>()
    protected val dynamicComponents
        get() = (component as? CachingPanel)?.dynamicChildren ?: component.dynamicChildren

    protected var childrenParams = listOf<T>()
        private set
    private var lastMeasure: List<Any>? = null

    val DynamicLayout.typedParams: T
        get() = layoutParams as T

    override fun hasChanged(): Boolean {
        return needsLayout
    }

    override fun requestLayout() {
        if (needsLayout) return
        needsLayout = true
    }

    private fun doRequestLayout() {
        if (isRootLayout()) {
            component.invalidate()
        } else {
            super.requestLayout()
        }
    }

    override fun doLayout(area: TerminalSize, components: List<Component>) {
        checkComponents()
        checkArea(area)
        if (!isRootLayout()) {
            return
        }
        if (needsLayout) {
            onLayout(area)
            needsLayout = false
        }
    }

    protected fun isRootLayout(): Boolean {
        return component.parent?.asDynamicLayout?.isCompatLayout ?: true
    }

    final override fun layout(area: TerminalSize, pos: TerminalPosition) {
        super.layout(area, pos)
        checkComponents()
        checkArea(area)
        if (needsLayout) {
            onLayout(area).also {
                lastMeasure = listOf(area, pos, components.map { it })
            }
            needsLayout = false
        }
    }

    final override fun measure(cols: MeasureSpec, rows: MeasureSpec): TerminalSize {
        checkComponents()
        return onMeasure(cols, rows).also {
            // lastMeasure = listOf(cols, rows, it, dynamicComponents.map { it::class.simpleName })
        }
    }

    override fun debugString(): String {
        return "${super.debugString()} $needsLayout $lastMeasure"
    }

    private fun checkArea(area: TerminalSize) {
        if (area != lastArea) {
            lastArea = area
            needsLayout = true
        }
    }

    private fun checkComponents() {
        val components = component.childrenList
        if (components != this.components) {
            this.components = components
            components.forEach(companion::coerce)
            cachedPreferredSize = null
            onChildrenChanged()
            needsLayout = true
        }
    }

    protected open fun onChildrenChanged() {
    }

    abstract fun onMeasure(cols: MeasureSpec, rows: MeasureSpec): TerminalSize
    abstract fun onLayout(area: TerminalSize)

    override fun getPreferredSize(components: List<Component>): TerminalSize {
        cachedPreferredSize?.let { return it }
        components.forEach(companion::coerce)
        return measure(
            MeasureSpec(MeasureType.AT_MOST, screen.terminalSize.columns),
            MeasureSpec(MeasureType.AT_MOST, screen.terminalSize.rows)
        ).also {
            cachedPreferredSize = it
        }
    }
}
