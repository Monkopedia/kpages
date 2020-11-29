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
import com.googlecode.lanterna.gui2.Container
import com.googlecode.lanterna.gui2.Panel
import java.util.WeakHashMap

/**
 * Allows for more complex constraints in UI layout, similar to the older
 * android system, such that multipass layouts could occur.
 *
 * This system only works with layout managers in this package, and will
 * fall back to the existing lanterna system when not available.
 */
interface DynamicLayout {
    val layoutParams: LayoutParams?
        get() = component.layoutData as? LayoutParams
    val component: Component
    val isCompatLayout: Boolean

    fun <T : LayoutParams> params(type: LayoutParams.LayoutCompanion<T>): T {
        return type.coerce(layoutParams)
    }

    fun requestLayout()
    fun measure(cols: MeasureSpec, rows: MeasureSpec): TerminalSize
    fun layout(area: TerminalSize, position: TerminalPosition)
    fun debugString(): String
}

interface HasDynamicLayout {
    val dynamicLayout: DynamicLayout
}

private val dynamicLayoutCache = WeakHashMap<Component, DynamicLayout>()

val Component.asDynamicLayout: DynamicLayout
    get() = dynamicLayoutCache.getOrPut(this) {
        (this as? HasDynamicLayout)?.dynamicLayout
            ?: ((this as? Panel)?.layoutManager as? DynamicLayout)
            ?: (this as? Border)?.let { BorderLayout(it) }
            ?: CompatDynamicLayout(this)
    }

val Container.dynamicChildren: Collection<DynamicLayout>
    get() = children.map(Component::asDynamicLayout)

fun Component.debugLayout(): String {
    return buildString {
        debugLayout(this)
    }
}

private fun Component.debugLayout(builder: StringBuilder, indent: String = "") {
    builder.apply {
        append(indent)
        append(asDynamicLayout.debugString())
        append(" ")
        append(layoutData)
        if (this@debugLayout is Container) {
            append(" {")
            append("\n")
            children.forEach {
                it.debugLayout(builder, "$indent  ")
            }
            append(indent)
            append("}")
        }
        append("\n")
    }
}
