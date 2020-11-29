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

data class Padding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val horizontal get() = left + right
    val vertical get() = top + bottom

    operator fun plus(size: TerminalSize): TerminalSize {
        return TerminalSize(size.columns + left + right, size.rows + top + bottom)
    }

    operator fun plus(size: TerminalPosition): TerminalPosition {
        return TerminalPosition(size.column + left, size.row + top)
    }

    companion object {
        val ZERO = Padding(0, 0, 0, 0)
    }
}

operator fun TerminalSize.plus(padding: Padding) = padding + this
operator fun TerminalSize.minus(padding: Padding) =
    TerminalSize(
        (columns - padding.left - padding.right).coerceAtLeast(0),
        (rows - padding.top - padding.bottom).coerceAtLeast(0)
    )

operator fun TerminalPosition.plus(padding: Padding) = padding + this
operator fun TerminalPosition.minus(padding: Padding) =
    TerminalSize(column - padding.left, row - padding.top)

operator fun TerminalPosition.minus(terminalPosition: TerminalPosition) =
    TerminalPosition(column - terminalPosition.column, row - terminalPosition.column)
