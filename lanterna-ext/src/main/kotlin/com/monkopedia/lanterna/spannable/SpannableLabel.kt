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
package com.monkopedia.lanterna.spannable

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.screen.TabBehaviour
import com.monkopedia.dynamiclayout.BaseDynamicLayout
import com.monkopedia.dynamiclayout.HasDynamicLayout
import com.monkopedia.dynamiclayout.MeasureSpec
import com.monkopedia.dynamiclayout.MeasureSpec.Companion.unspecified
import com.monkopedia.lanterna.Selectable
import com.monkopedia.lanterna.SelectableContainer
import com.monkopedia.lanterna.SelectableListener
import com.monkopedia.lanterna.spannable.StyleSpan.Companion.withSpans
import java.util.ArrayList
import java.util.LinkedList

class SpannableLabel(text: CharSequence) :
    Label(text.toString()),
    HasDynamicLayout,
    SelectableContainer {

    private var csLines: Array<CharSequence> = emptyArray()
        set(value) {
            field = value
            cachedWrappedText = null
        }

    override val dynamicLayout = SpannableLayoutManager(this)
    private var isInitialized = true

    override val selectableListeners = mutableListOf<SelectableListener>()

    init {
        setText(text)
    }

    private var cachedWrappedText: List<CharSequence>? = null
    val wrappedText: List<CharSequence>
        get() {
            return cachedWrappedText ?: getWordWrappedText(size.columns, *csLines).also {
                cachedWrappedText = it
            }
        }

    fun setText(text: CharSequence) {
        var text = text
        while (text.contains("\r")) {
            val index = text.indexOf("\r")
            text = Spanned(text.subSequence(0, index), text.subSequence(index + 1, text.length))
        }
        csLines = text.splitSequence('\n')
        dynamicLayout.requestLayout()
        updateActions()
    }

    override fun setSize(size: TerminalSize?): Label {
        if (size != this.size) {
            return super.setSize(size).also {
                cachedWrappedText = null
                updateActions()
            }
        }
        return super.setSize(size)
    }

    override fun setText(text: String) {
        if (isInitialized) {
            setText(text as CharSequence)
        }
    }

    fun getCharSequence(): CharSequence {
        return csLines.toList().joinToSpanned("\n")
    }

    override fun getText(): String {
        return getCharSequence().toString()
    }

    override fun setPosition(position: TerminalPosition?): Label {
        if (position != this.position) {
            return super.setPosition(position).also {
                updateActions()
            }
        }
        return super.setPosition(position)
    }

    private fun updateActions() {
        if (selectableListeners.isEmpty()) return
        val actions = currentSelectables
        selectableListeners.forEach {
            it.onSelectablesChanged(this, actions)
        }
    }

    override val currentSelectables: List<Pair<TerminalPosition, Selectable>>
        get() {
            val actions = mutableListOf<Pair<TerminalPosition, Selectable>>()
            val linesToDraw = wrappedText
            for (row in 0 until size.rows.coerceAtMost(linesToDraw.size)) {
                when (val line = linesToDraw[row]) {
                    is Spanned -> {
                        actions.addAll(
                            line.getAllSpans().filter { it.span is Selectable }.map {
                                TerminalPosition(it.start, row) to (it.span as Selectable)
                            }
                        )
                    }
                    is StaticSpan -> {
                        for (span in line.spans.filterIsInstance<LinkSpan>()) {
                            actions.add(TerminalPosition(0, row) to span)
                        }
                    }
                }
            }
            return actions
        }

    class SpannableLayoutManager(override val component: SpannableLabel) : BaseDynamicLayout() {
        private var lastMeasure: List<Any>? = null
        override fun measure(cols: MeasureSpec, rows: MeasureSpec): TerminalSize {
            val maxWidth = component.csLines.maxOfOrNull { getColumnWidth(it) } ?: 0
            val width = cols.size(maxWidth)
            return if (maxWidth > width) {
                val wordWrapped = getWordWrappedText(width, *component.csLines)
                TerminalSize(width, wordWrapped.size)
            } else {
                TerminalSize(width, component.csLines.size)
            }.also {
                lastMeasure = listOf(cols, rows, it)
            }
        }

        internal fun calcSize(): TerminalSize {
            return measure(unspecified(Int.MAX_VALUE), unspecified(Int.MAX_VALUE))
        }

        override fun debugString(): String {
            return super.debugString() + " ${
            component.text.let {
                it.substring(0, it.length.coerceAtMost(10)).replace("\n", "\\n")
            }
            }"
        }
    }

    override fun createDefaultRenderer(): ComponentRenderer<Label> {
        return object : ComponentRenderer<Label> {
            override fun getPreferredSize(label: Label): TerminalSize {
                return (label as SpannableLabel).dynamicLayout.calcSize()
            }

            override fun drawComponent(
                graphics: TextGUIGraphics,
                component: Label
            ) {
                component as SpannableLabel
                val themeDefinition = component.themeDefinition
                graphics.applyThemeStyle(themeDefinition.normal)
                if (foregroundColor != null) {
                    graphics.foregroundColor = foregroundColor
                }
                if (backgroundColor != null) {
                    graphics.backgroundColor = backgroundColor
                }
                val linesToDraw = component.wrappedText
                for (row in 0 until graphics.size.rows.coerceAtMost(linesToDraw.size)) {
                    when (val line = linesToDraw[row]) {
                        is Spanned -> {
                            var index = 0
                            val staticSpans = line.toStaticSpans()
                            for (i in staticSpans.indices) {
                                val segment = staticSpans[i]
                                graphics.withSpans(segment.spans) {
                                    val end = (graphics.size.columns - index).coerceAtMost(
                                        segment.length
                                    )
                                    if (end < 0) return@withSpans
                                    graphics.putString(
                                        index,
                                        row,
                                        segment.toString().substring(0, end)
                                    )
                                    if (i == staticSpans.size - 1) {
                                        for (i in (index + end) until graphics.size.columns) {
                                            graphics.setCharacter(i, row, ' ')
                                        }
                                    }
                                }
                                index += segment.length
                            }
                        }
                        is StaticSpan -> {
                            graphics.withSpans(line.spans) {
                                val end = (graphics.size.columns).coerceAtMost(line.length)
                                graphics.putString(0, row, line.toString().substring(0, end))
                                for (i in end until graphics.size.columns) {
                                    graphics.setCharacter(i, row, ' ')
                                }
                            }
                        }
                        else -> {
                            val end = (graphics.size.columns).coerceAtMost(line.length)
                            graphics.putString(0, row, line.toString().substring(0, end))
                            for (i in end until graphics.size.columns) {
                                graphics.setCharacter(i, row, ' ')
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun CharSequence.splitSequence(c: Char): Array<CharSequence> {
    val indexes = withIndex().filter { it.value == c }.map { it.index }
    return (listOf(0) + indexes + length).zipWithNext().map {
        subSequence(if (it.first == 0) 0 else it.first + 1, it.second)
    }.toTypedArray()
}

fun getWordWrappedText(
    maxWidth: Int,
    vararg lines: CharSequence
): List<CharSequence> {
    // Bounds checking
    if (maxWidth <= 0) {
        return listOf(*lines)
    }
    val result: MutableList<CharSequence> =
        ArrayList()
    val linesToBeWrapped =
        LinkedList(listOf(*lines))
    while (linesToBeWrapped.isNotEmpty()) {
        val row = linesToBeWrapped.removeFirst()
        val rowWidth = TerminalTextUtils.getColumnWidth(row.toString())
        if (rowWidth <= maxWidth) {
            result.add(row)
        } else {
            // Now search in reverse and find the first possible line-break
            val characterIndexMax =
                TerminalTextUtils.getStringCharacterIndex(row.toString(), maxWidth)
            var characterIndex = characterIndexMax
            while (characterIndex >= 0 &&
                !Character.isSpaceChar(row[characterIndex]) &&
                !TerminalTextUtils.isCharCJK(row[characterIndex])
            ) {
                characterIndex--
            }
            // right *after* a CJK is also a "nice" spot to break the line!
            if (characterIndex in 0 until characterIndexMax &&
                TerminalTextUtils.isCharCJK(row[characterIndex])
            ) {
                characterIndex++ // with these conditions it fits!
            }
            if (characterIndex < 0) {
                // Failed! There was no 'nice' place to cut so just cut it at maxWidth
                characterIndex = characterIndexMax.coerceAtLeast(1) // at least 1 char
                result.add(row.subSequence(0, characterIndex))
                linesToBeWrapped.addFirst(row.subSequence(characterIndex, row.length))
            } else {
                // characterIndex == 0 only happens, if either
                //   - first char is CJK and maxWidth==1   or
                //   - first char is whitespace
                // either way: put it in row before break to prevent infinite loop.
                characterIndex = characterIndex.coerceAtLeast(1) // at least 1 char

                // Ok, split the row, add it to the result and continue processing the second half on a new line
                result.add(row.subSequence(0, characterIndex))
                while (characterIndex < row.length &&
                    Character.isSpaceChar(row[characterIndex])
                ) {
                    characterIndex++
                }
                if (characterIndex < row.length) { // only if rest contains non-whitespace
                    linesToBeWrapped.addFirst(row.subSequence(characterIndex, row.length))
                }
            }
        }
    }
    return result
}

fun getColumnWidth(s: CharSequence): Int {
    return getColumnIndex(s, s.length)
}

fun getColumnIndex(s: CharSequence, stringCharacterIndex: Int): Int {
    return getColumnIndex(s, stringCharacterIndex, TabBehaviour.CONVERT_TO_FOUR_SPACES, -1)
}

fun getColumnIndex(
    s: CharSequence,
    stringCharacterIndex: Int,
    tabBehaviour: TabBehaviour,
    firstCharacterColumnPosition: Int
): Int {
    var index = 0
    for (i in 0 until stringCharacterIndex) {
        if (s[i] == '\t') {
            index += tabBehaviour.getTabReplacement(firstCharacterColumnPosition).length
        } else {
            if (TerminalTextUtils.isCharCJK(s[i])) {
                index++
            }
            index++
        }
    }
    return index
}

private fun Collection<CharSequence>.joinToSpanned(separator: CharSequence): CharSequence {
    return Spanned().also {
        for ((i, item) in withIndex()) {
            if (item.isEmpty()) continue
            if (i != 0) {
                it.append(separator)
            }
            it.append(item)
        }
    }
}
