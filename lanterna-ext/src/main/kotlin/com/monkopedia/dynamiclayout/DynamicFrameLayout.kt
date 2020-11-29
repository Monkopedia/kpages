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
import com.googlecode.lanterna.gui2.Panel
import com.monkopedia.dynamiclayout.MeasureSpec.Companion.exactly

enum class DirectionGravity {
    START,
    CENTER,
    END;

    fun space(childSize: Int, areaSize: Int): Int = when (this) {
        START -> 0
        CENTER -> (areaSize - childSize) / 2
        END -> (areaSize - childSize)
    }
}

enum class Gravity(val vertical: DirectionGravity, val horizontal: DirectionGravity) {
    TOP_LEFT(DirectionGravity.START, DirectionGravity.START),
    TOP_CENTER(DirectionGravity.START, DirectionGravity.CENTER),
    TOP_RIGHT(DirectionGravity.START, DirectionGravity.END),
    CENTER_LEFT(DirectionGravity.CENTER, DirectionGravity.START),
    CENTER(DirectionGravity.CENTER, DirectionGravity.CENTER),
    CENTER_RIGHT(DirectionGravity.CENTER, DirectionGravity.END),
    BOTTOM_LEFT(DirectionGravity.END, DirectionGravity.START),
    BOTTOM_CENTER(DirectionGravity.END, DirectionGravity.CENTER),
    BOTTOM_RIGHT(DirectionGravity.END, DirectionGravity.END),
}

open class GravityLayoutParams(columns: SizeSpec, rows: SizeSpec, val gravity: Gravity) :
    LayoutParams(columns, rows) {

    companion object : LayoutCompanion<GravityLayoutParams>(GravityLayoutParams::class) {
        override fun createDefault(): GravityLayoutParams =
            GravityLayoutParams(Wrap, Wrap, Gravity.TOP_LEFT)

        override fun convert(params: LayoutParams): GravityLayoutParams =
            (params as? WeightedLayoutParams)?.let {
                GravityLayoutParams(params.cols, params.rows, params.gravity)
            } ?: GravityLayoutParams(params.cols, params.rows, Gravity.TOP_LEFT)
    }

    override fun toString(): String {
        return "GravityLayoutParams($cols, $rows, $gravity)"
    }
}

class DynamicFrameLayout(target: Panel) :
    DynamicLayoutManager<GravityLayoutParams>(GravityLayoutParams, target) {
    var padding: Padding = Padding.ZERO
        set(value) {
            field = value
            requestLayout()
        }

    override fun onLayout(area: TerminalSize) {
        val paddedArea = area - padding
        val withSizes =
            dynamicComponents.measureChildren(exactly(area.columns), exactly(area.rows))
        for ((child, size) in withSizes) {
            val params = child.typedParams
            child.layout(
                size,
                TerminalPosition(
                    params.gravity.horizontal.space(size.columns, paddedArea.columns),
                    params.gravity.vertical.space(size.rows, paddedArea.rows)
                ) + padding
            )
        }
    }

    override fun onMeasure(cols: MeasureSpec, rows: MeasureSpec): TerminalSize {
        val sizes = dynamicComponents.measureChildren(cols, rows).map { it.second }
        return TerminalSize(
            cols.size((sizes.maxOfOrNull { it.columns } ?: 0) + padding.horizontal),
            rows.size((sizes.maxOfOrNull { it.rows } ?: 0) + padding.vertical)
        )
    }

    private inline fun Collection<DynamicLayout>.measureChildren(
        cols: MeasureSpec,
        rows: MeasureSpec
    ): List<Pair<DynamicLayout, TerminalSize>> {
        val childCols = cols - padding.horizontal
        val childRows = rows - padding.vertical
        return map {
            val params = it.typedParams
            it to it.measure(params.cols.forChild(childCols), params.rows.forChild(childRows))
        }
    }
}
