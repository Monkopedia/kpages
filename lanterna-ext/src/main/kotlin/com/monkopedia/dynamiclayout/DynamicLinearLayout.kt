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
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.monkopedia.dynamiclayout.MeasureSpec.Companion.atMost
import com.monkopedia.dynamiclayout.MeasureSpec.Companion.exactly
import kotlin.math.roundToInt

class WeightedLayoutParams(
    columns: SizeSpec,
    rows: SizeSpec,
    gravity: Gravity = Gravity.TOP_LEFT,
    val weight: Int = 0
) : GravityLayoutParams(columns, rows, gravity) {

    companion object : LayoutCompanion<WeightedLayoutParams>(WeightedLayoutParams::class) {
        override fun createDefault(): WeightedLayoutParams =
            WeightedLayoutParams(Wrap, Wrap)

        override fun convert(params: LayoutParams): WeightedLayoutParams =
            (params as? GravityLayoutParams)?.let {
                WeightedLayoutParams(it.cols, it.rows, it.gravity)
            } ?: WeightedLayoutParams(params.cols, params.rows)
    }

    override fun toString(): String {
        return "LinearLayoutParams($cols, $rows, $gravity, $weight)"
    }
}

fun DynamicLinearLayout(direction: Direction, target: Panel) = when (direction) {
    Direction.HORIZONTAL -> DynamicHorizontalLinearLayout(target)
    Direction.VERTICAL -> DynamicVerticalLinearLayout(target)
}

class DynamicHorizontalLinearLayout(target: Panel) :
    DynamicLayoutManager<WeightedLayoutParams>(WeightedLayoutParams, target) {
    var padding: Padding = Padding.ZERO
        set(value) {
            field = value
            requestLayout()
        }
    val renderer = DynamicHorizontalLayoutRenderer().also {
        component.renderer = it
    }

    override fun onLayout(area: TerminalSize) {
        if (dynamicComponents.isEmpty()) return
        val totalWeight = dynamicComponents.sumBy { it.typedParams.weight }
        val paddedArea = area - padding
        var withSizes =
            dynamicComponents.measureChildren(exactly(paddedArea.columns), exactly(paddedArea.rows))
        val extraSpace = if (totalWeight > 0) withSizes.last().let {
            area.columns - (it.third + it.second.columns)
        } else 0
        if (extraSpace > 0) {
            withSizes = withSizes.secondMeasure(totalWeight, extraSpace)
        }
        for ((child, size, pos) in withSizes) {
            val params = child.params(GravityLayoutParams)
            child.layout(
                size,
                TerminalPosition(
                    pos,
                    params.gravity.vertical.space(size.rows, paddedArea.columns)
                ) + padding
            )
        }
    }

    override fun onMeasure(cols: MeasureSpec, rows: MeasureSpec): TerminalSize {
        val totalWeight = dynamicComponents.sumBy { it.typedParams.weight }
        val sizes = dynamicComponents.measureChildren(cols, rows).map { it }
        return TerminalSize(
            cols.size(
                if (totalWeight != 0) {
                    Int.MAX_VALUE
                } else {
                    sizes.lastOrNull()?.let { it.third + it.second.columns }
                        ?.plus(padding.horizontal) ?: 0
                }
            ),
            rows.size(
                sizes.maxOfOrNull { it.second.rows }?.plus(padding.vertical) ?: 0
            )
        )
    }

    private inline fun Collection<DynamicLayout>.measureChildren(
        cols: MeasureSpec,
        rows: MeasureSpec
    ): List<Triple<DynamicLayout, TerminalSize, Int>> {
        val maxCols = cols.size(Int.MAX_VALUE)
        var position = 0
        val childRows = rows - padding.vertical
        return map {
            val params = it.typedParams
            val childCols = atMost((maxCols - position).coerceAtLeast(0))
            val childSize = it.measure(
                params.cols.forChild(childCols),
                params.rows.forChild(childRows)
            )
            Triple(it, childSize, position).also {
                position += childSize.columns
            }
        }
    }

    private inline fun List<Triple<DynamicLayout, TerminalSize, Int>>.secondMeasure(
        totalWeight: Int,
        extraSpace: Int
    ): List<Triple<DynamicLayout, TerminalSize, Int>> {
        var position = 0
        return map { (component, size, _) ->
            val weight = component.typedParams.weight
            (
                if (weight == 0) Triple(component, size, position)
                else {
                    val childCols = exactly(
                        size.columns + (extraSpace * (weight.toDouble() / totalWeight)).roundToInt()
                    )
                    val childRows = exactly(size.rows)
                    Triple(component, component.measure(childCols, childRows), position)
                }
                ).also {
                position += it.second.columns
            }
        }
    }
}

class DynamicVerticalLinearLayout(target: Panel) :
    DynamicLayoutManager<WeightedLayoutParams>(WeightedLayoutParams, target) {
    var padding: Padding = Padding.ZERO
        set(value) {
            field = value
            requestLayout()
        }
    val renderer = DynamicVerticalLayoutRenderer().also {
        component.renderer = it
    }

    override fun onLayout(area: TerminalSize) {
        if (dynamicComponents.isEmpty()) return
        val totalWeight = dynamicComponents.sumBy { it.typedParams.weight }
        val paddedArea = area - padding
        var withSizes =
            dynamicComponents.measureChildren(exactly(area.columns), exactly(area.rows))
        val extraSpace = if (totalWeight > 0) withSizes.last().let {
            area.rows - (it.third + it.second.rows)
        } else 0
        if (extraSpace > 0) {
            withSizes = withSizes.secondMeasure(totalWeight, extraSpace)
        }
        for ((child, size, pos) in withSizes) {
            val params = child.params(GravityLayoutParams)
            child.layout(
                size,
                TerminalPosition(
                    params.gravity.horizontal.space(size.columns, paddedArea.columns), pos
                ) + padding
            )
        }
    }

    override fun onMeasure(cols: MeasureSpec, rows: MeasureSpec): TerminalSize {
        val totalWeight = dynamicComponents.sumBy { it.typedParams.weight }
        val sizes = dynamicComponents.measureChildren(cols, rows)
        return TerminalSize(
            cols.size(
                sizes.maxOfOrNull { it.second.columns }?.plus(padding.horizontal)
                    ?: 0
            ),
            rows.size(
                if (totalWeight != 0) {
                    Int.MAX_VALUE
                } else {
                    sizes.lastOrNull()?.let { it.third + it.second.rows }
                        ?.plus(padding.vertical) ?: 0
                }
            )
        )
    }

    private inline fun Collection<DynamicLayout>.measureChildren(
        cols: MeasureSpec,
        rows: MeasureSpec
    ): List<Triple<DynamicLayout, TerminalSize, Int>> {
        val maxRows = (rows - padding.vertical).size(Int.MAX_VALUE)
        var position = 0
        val childCols = cols - padding.horizontal
        return map {
            val params = it.typedParams
            val childRows = atMost((maxRows - position).coerceAtLeast(0))
            val childSize = it.measure(
                params.cols.forChild(childCols),
                params.rows.forChild(childRows)
            )
            Triple(it, childSize, position).also {
                position += childSize.rows
            }
        }
    }

    private inline fun List<Triple<DynamicLayout, TerminalSize, Int>>.secondMeasure(
        totalWeight: Int,
        extraSpace: Int
    ): List<Triple<DynamicLayout, TerminalSize, Int>> {
        var position = 0
        return map { (component, size, _) ->
            val weight = component.typedParams.weight
            (
                if (weight == 0) Triple(component, size, position)
                else {
                    val childCols = exactly(size.columns)
                    val childRows = exactly(
                        size.rows + (extraSpace * (weight.toDouble() / totalWeight)).roundToInt()
                    )
                    Triple(component, component.measure(childCols, childRows), position)
                }
                ).also {
                position += it.second.rows
            }
        }
    }
}

class DynamicVerticalLayoutRenderer : ComponentRenderer<Panel> {
    private var fillAreaBeforeDrawingComponents = true

    fun setFillAreaBeforeDrawingComponents(fillAreaBeforeDrawingComponents: Boolean) {
        this.fillAreaBeforeDrawingComponents = fillAreaBeforeDrawingComponents
    }

    override fun getPreferredSize(panel: Panel): TerminalSize {
        return panel.layoutManager.getPreferredSize(panel.childrenList)
    }

    override fun drawComponent(graphics: TextGUIGraphics, panel: Panel) {
        val components = panel.childrenList
        if (panel.isInvalid) {
            panel.layoutManager.doLayout(graphics.size, components)
        }
        val screen = graphics.textGUI.screen.terminalSize
        val topLeft = -panel.toGlobal(TerminalPosition.TOP_LEFT_CORNER)
        val bottomRight = (topLeft + screen)

        if (fillAreaBeforeDrawingComponents) {
            // Reset the area
            graphics.applyThemeStyle(panel.themeDefinition.normal)
            if (panel.fillColorOverride != null) {
                graphics.backgroundColor = panel.fillColorOverride
            }
            graphics.fill(' ')
        }

        for (child in components) {
            val start = child.position.row
            val end = start + child.size.rows
            if (end < topLeft.row) {
                continue
            }
            if (start > bottomRight.row + 1) {
                break
            }
            val componentGraphics =
                graphics.newTextGraphics(child.position, child.size)
            child.draw(componentGraphics)
        }
    }
}

class DynamicHorizontalLayoutRenderer : ComponentRenderer<Panel> {
    var fillAreaBeforeDrawingComponents = true

    override fun getPreferredSize(panel: Panel): TerminalSize {
        return panel.layoutManager.getPreferredSize(panel.childrenList)
    }

    override fun drawComponent(graphics: TextGUIGraphics, panel: Panel) {
        val components = panel.childrenList
        if (panel.isInvalid) {
            panel.layoutManager.doLayout(graphics.size, components)
        }

        val screen = graphics.textGUI.screen.terminalSize
        val topLeft = -panel.toGlobal(TerminalPosition.TOP_LEFT_CORNER)
        val bottomRight = (topLeft + screen)

        if (fillAreaBeforeDrawingComponents) {
            // Reset the area
            graphics.applyThemeStyle(panel.themeDefinition.normal)
            if (panel.fillColorOverride != null) {
                graphics.backgroundColor = panel.fillColorOverride
            }
            graphics.fill(' ')
        }
        for (child in components) {
            val start = child.position.column
            val end = start + child.size.columns
            if (end < topLeft.column) {
                continue
            }
            if (start > bottomRight.column) {
                break
            }
            val componentGraphics =
                graphics.newTextGraphics(child.position, child.size)
            child.draw(componentGraphics)
        }
    }
}

operator fun TerminalPosition.unaryMinus(): TerminalPosition {
    return TerminalPosition(-column, -row)
}

operator fun TerminalPosition.plus(size: TerminalSize): TerminalPosition {
    return TerminalPosition(column + size.columns, row + size.rows)
}

operator fun TerminalPosition.plus(pos: TerminalPosition): TerminalPosition {
    return TerminalPosition(column + pos.column, row + pos.row)
}
