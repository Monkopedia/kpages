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

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.monkopedia.dynamiclayout.MeasureSpec.Companion.atMost
import com.monkopedia.dynamiclayout.MeasureSpec.Companion.exactly
import com.monkopedia.util.logger
import kotlin.math.roundToInt

inline class GridRow(val items: List<DynamicLayout>)
inline class GridColumn(val items: List<DynamicLayout>)

private val LOGGER = DynamicGridLayout::class.logger

class DynamicGridLayout(component: Panel, cols: Int) :
    DynamicLayoutManager<WeightedLayoutParams>(WeightedLayoutParams, component) {
    private var cachedMeasure: Pair<TerminalSize, MeasureData>? = null
    var padding: Padding = Padding.ZERO
        set(value) {
            field = value
            requestLayout()
        }
    var cols = cols
        set(value) {
            if (field == value) return
            field = value
            onChildrenChanged()
        }
    lateinit var rows: List<GridRow>
    lateinit var columns: List<GridColumn>

    init {
        component.renderer = DynamicGridLayoutRenderer()
    }

    private data class RowMeasureData(
        val rowSize: Int,
        val position: Int,
        val weight: Int,
        val measurements: List<TerminalSize>
    )

    private data class ColumnMeasureData(
        val columnSize: Int,
        val position: Int,
        val weight: Int,
        val measurements: List<TerminalSize>
    )

    private data class MeasureData(
        val rowData: List<RowMeasureData>,
        val columnData: List<ColumnMeasureData>
    )

    init {
        onChildrenChanged()
    }

    override fun onChildrenChanged() {
        if (dynamicComponents.size.rem(cols) != 0) {
            LOGGER.warn(
                "Wrong number of children, ${dynamicComponents.size} not divisible by $cols"
            )
        }
        rows = dynamicComponents.chunked(cols) { GridRow(it.toList()) }
        columns = (0 until cols).map { i -> GridColumn(rows.map { it.items[i] }) }
    }

    override fun onMeasure(cols: MeasureSpec, rows: MeasureSpec): TerminalSize {
        val data = measureChildren(cols, rows)
        return TerminalSize(
            cols.size(
                (data.columnData.lastOrNull()?.let { it.columnSize + it.position } ?: 0) +
                    1 + padding.horizontal
            ),
            rows.size(
                (data.rowData.lastOrNull()?.let { it.rowSize + it.position } ?: 0) +
                    1 + padding.vertical
            )
        ).also {
            cachedMeasure = it to data
        }
    }

    override fun onLayout(area: TerminalSize) {
        val data = measureDataFor(area)

        for ((index, child) in dynamicComponents.withIndex()) {
            val colIndex = index.rem(this.cols)
            val rowIndex = index / this.cols
            val col = data.columnData[colIndex]
            val row = data.rowData[rowIndex]
            val size = row.measurements[colIndex]
            val xPos = col.position +
                child.typedParams.gravity.horizontal.space(size.columns, col.columnSize)
            val yPos = row.position +
                child.typedParams.gravity.vertical.space(size.rows, row.rowSize)
            child.layout(size, TerminalPosition(xPos, yPos))
        }
    }

    private fun measureDataFor(area: TerminalSize): MeasureData {
        return if (cachedMeasure?.first == area) cachedMeasure!!.second
        else measureChildren(exactly(area.columns), exactly(area.rows)).also {
            cachedMeasure = area to it
        }
    }

    private fun measureChildren(cols: MeasureSpec, rows: MeasureSpec): MeasureData {
        val totalWeight = dynamicComponents.maxOfOrNull { it.typedParams.weight }?.toDouble() ?: 0.0
        val childRows = rows - padding.vertical
        val maxCols = (cols - padding.horizontal).size(Int.MAX_VALUE)
        val maxRows = childRows.size(Int.MAX_VALUE)
        var position = 0
        var columnData = this.columns.map { column ->
            val childCols = atMost((maxCols - position).coerceAtLeast(0))
            column.measureColumn(
                Wrap.forChild(childCols),
                Wrap.forChild(childRows)
            ).also {
                position += it.columnSize
            }
        }.mapIf(position < maxCols && maxCols != Int.MAX_VALUE) {
            val extraSpace = maxCols - position
            if (it.weight > 0) {
                it.copy(
                    columnSize = it.columnSize +
                        ((it.weight / totalWeight) * extraSpace).roundToInt()
                )
            } else {
                it
            }
        }.also {
            position = 1
        }.map { col ->
            col.copy(position = position).also {
                position += col.columnSize + 1
            }
        }
        position = 0
        var rowData = this.rows.map {
            val childRows = atMost((maxRows - position).coerceAtLeast(0))
            it.measureRow(columnData, childRows).also {
                position += it.rowSize
            }
        }.mapIf(position < maxRows && maxRows != Int.MAX_VALUE) {
            val extraSpace = maxRows - position
            if (it.weight > 0) {
                it.copy(
                    rowSize = it.rowSize +
                        ((it.weight / totalWeight) * extraSpace).roundToInt()
                )
            } else {
                it
            }
        }.also {
            position = 1
        }.map { row ->
            row.copy(position = position).also {
                position += row.rowSize + 1
            }
        }
        return MeasureData(
            rowData,
            columnData
        )
    }

    private fun GridRow.measureRow(
        cols: List<ColumnMeasureData>,
        rows: MeasureSpec
    ): RowMeasureData {
        var position = 0
        val childRows = rows
        val childrenSizes = items.zip(cols).map { (item, col) ->
            val params = item.typedParams
            val childCols = atMost(col.columnSize.coerceAtLeast(0))
            item.measure(
                params.cols.forChild(childCols),
                params.rows.forChild(childRows)
            ).also {
                position += it.columns
            }
        }
        return RowMeasureData(
            determineRowSize(rows, childrenSizes),
            0,
            items.sumBy { it.typedParams.weight },
            childrenSizes
        )
    }

    private fun determineRowSize(rows: MeasureSpec, childrenSizes: List<TerminalSize>): Int {
        return rows.size(
            childrenSizes.maxOfOrNull { it.rows } ?: 0
        )
    }

    private fun GridColumn.measureColumn(cols: MeasureSpec, rows: MeasureSpec): ColumnMeasureData {
        val maxRows = rows.size(Int.MAX_VALUE)
        var position = 0
        val childCols = cols
        val childrenSizes = items.map { item ->
            val params = item.typedParams
            val childRows = atMost((maxRows - position).coerceAtLeast(0))
            item.measure(
                params.cols.forChild(childCols),
                params.rows.forChild(childRows)
            ).also {
                position += it.rows
            }
        }
        return ColumnMeasureData(
            determineColumnSize(cols, childrenSizes),
            0,
            items.sumBy { it.typedParams.weight },
            childrenSizes
        )
    }

    private fun determineColumnSize(cols: MeasureSpec, childrenSizes: List<TerminalSize>): Int {
        return cols.size(
            childrenSizes.maxOfOrNull { it.columns } ?: 0
        )
    }

    fun drawBorder(graphics: TextGUIGraphics) {
        val measureData = measureDataFor(graphics.size)

        // Top
        drawHorizontal(
            graphics,
            measureData,
            0,
            Symbols.SINGLE_LINE_TOP_LEFT_CORNER,
            Symbols.SINGLE_LINE_T_DOWN,
            Symbols.SINGLE_LINE_TOP_RIGHT_CORNER
        )
        // Bottom
        drawHorizontal(
            graphics,
            measureData,
            measureData.rowData.lastOrNull()?.let { it.position + it.rowSize } ?: 0,
            Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER,
            Symbols.SINGLE_LINE_T_UP,
            Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER
        )

        for (row in measureData.rowData) {
            if (row.position == 1) continue
            drawHorizontal(
                graphics,
                measureData,
                row.position - 1,
                Symbols.SINGLE_LINE_T_RIGHT,
                Symbols.SINGLE_LINE_CROSS,
                Symbols.SINGLE_LINE_T_LEFT
            )
        }

        drawVertical(graphics, measureData, 0)
        for (col in measureData.columnData) {
            drawVertical(graphics, measureData, col.position + col.columnSize)
        }
    }

    private fun drawHorizontal(
        graphics: TextGUIGraphics,
        measureData: MeasureData,
        yPos: Int,
        startChar: Char,
        crossChar: Char,
        endChar: Char
    ) {
        for (column in measureData.columnData) {
            graphics.setCharacter(
                column.position - 1, yPos,
                if (column.position != 1) crossChar
                else startChar
            )
            for (i in ((0 until column.columnSize).map((column.position)::plus))) {
                graphics.setCharacter(i, yPos, Symbols.SINGLE_LINE_HORIZONTAL)
            }
        }
        val lastCol = measureData.columnData.lastOrNull()?.let { it.position + it.columnSize }
            ?: return
        graphics.setCharacter(lastCol, yPos, endChar)
    }

    private fun drawVertical(
        graphics: TextGUIGraphics,
        measureData: MeasureData,
        xPos: Int
    ) {
        for (row in measureData.rowData) {
            for (i in ((0 until row.rowSize).map((row.position)::plus))) {
                graphics.setCharacter(xPos, i, Symbols.SINGLE_LINE_VERTICAL)
            }
        }
    }
}

inline fun <T> Collection<T>.mapIf(condition: Boolean, transform: (T) -> T): Collection<T> {
    return if (condition) {
        map(transform)
    } else this
}

class DynamicGridLayoutRenderer : ComponentRenderer<Panel> {
    var fillAreaBeforeDrawingComponents = true

    override fun getPreferredSize(panel: Panel): TerminalSize {
        return panel.layoutManager.getPreferredSize(panel.childrenList)
    }

    override fun drawComponent(graphics: TextGUIGraphics, panel: Panel) {
        val components = panel.childrenList
        if (panel.isInvalid) {
            panel.layoutManager.doLayout(graphics.size, components)
        }

        if (fillAreaBeforeDrawingComponents) {
            // Reset the area
            graphics.applyThemeStyle(panel.themeDefinition.normal)
            if (panel.fillColorOverride != null) {
                graphics.backgroundColor = panel.fillColorOverride
            }
            graphics.fill(' ')
        }
        (panel.layoutManager as? DynamicGridLayout)?.drawBorder(graphics)
        for (child in components) {
            val componentGraphics =
                graphics.newTextGraphics(child.position, child.size)
            child.draw(componentGraphics)
        }
    }
}
