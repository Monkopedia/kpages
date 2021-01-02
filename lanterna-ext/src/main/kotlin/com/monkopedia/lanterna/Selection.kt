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

import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.monkopedia.lanterna.navigation.Navigation

interface EventMatcher {
    fun matches(keyStroke: KeyStroke): Boolean

    override fun toString(): String
    override fun equals(other: Any?): Boolean

    companion object {
        object AltDown : EventMatcher {
            override fun matches(keyStroke: KeyStroke): Boolean = keyStroke.isAltDown
            override fun toString(): String = "Alt"
            override fun equals(other: Any?): Boolean = other == AltDown
        }

        object AltUp : EventMatcher {
            override fun matches(keyStroke: KeyStroke): Boolean = keyStroke.isAltDown
            override fun toString(): String = ""
            override fun equals(other: Any?): Boolean = other == AltUp
        }

        object CtrlDown : EventMatcher {
            override fun matches(keyStroke: KeyStroke): Boolean = keyStroke.isCtrlDown
            override fun toString(): String = "Ctrl"
            override fun equals(other: Any?): Boolean = other == CtrlDown
        }

        object CtrlUp : EventMatcher {
            override fun matches(keyStroke: KeyStroke): Boolean = !keyStroke.isCtrlDown
            override fun toString(): String = ""
            override fun equals(other: Any?): Boolean = other == CtrlUp
        }

        object ShiftDown : EventMatcher {
            override fun matches(keyStroke: KeyStroke): Boolean = keyStroke.isShiftDown
            override fun toString(): String = "Shift"
            override fun equals(other: Any?): Boolean = other == ShiftDown
        }

        object ShiftUp : EventMatcher {
            override fun matches(keyStroke: KeyStroke): Boolean = !keyStroke.isShiftDown
            override fun toString(): String = ""
            override fun equals(other: Any?): Boolean = other == ShiftUp
        }

        private class KeyTypeMatcher(private val type: KeyType) : EventMatcher {
            override fun matches(keyStroke: KeyStroke) = keyStroke.keyType == type
            override fun toString(): String = type.toString()
            override fun equals(other: Any?): Boolean = (other as? KeyTypeMatcher)?.type == type
        }

        fun keyType(type: KeyType): EventMatcher = KeyTypeMatcher(type)

        private class CharMatcher(private val char: Char) : EventMatcher {
            override fun matches(keyStroke: KeyStroke) =
                keyStroke.keyType == KeyType.Character && keyStroke.character == char
            override fun toString(): String = "'$char'"
            override fun equals(other: Any?): Boolean = (other as? CharMatcher)?.char == char
        }

        fun character(char: Char): EventMatcher = CharMatcher(char)

        fun Char.matcher() = character(this)
        fun KeyType.matcher() = keyType(this)

        private class AndMatcher(
            private val first: EventMatcher,
            private val second: EventMatcher
        ) : EventMatcher {
            override fun matches(keyStroke: KeyStroke): Boolean =
                first.matches(keyStroke) && second.matches(keyStroke)
            override fun toString(): String = buildString {
                append(first.toString())
                if (isNotEmpty()) {
                    append(" + ")
                }
                append(second.toString())
            }

            override fun equals(other: Any?): Boolean = (other as? AndMatcher)?.let { o ->
                (o.first == first && o.second == second) ||
                    (o.second == first && o.first == second)
            } ?: false
        }

        infix fun EventMatcher.and(other: EventMatcher): EventMatcher = AndMatcher(this, other)

        private class OrMatcher(
            private val first: EventMatcher,
            private val second: EventMatcher
        ) : EventMatcher {
            override fun matches(keyStroke: KeyStroke): Boolean =
                first.matches(keyStroke) || second.matches(keyStroke)
            override fun toString(): String = buildString {
                append(first.toString())
                if (isNotEmpty()) {
                    append("\n")
                }
                append(second.toString())
            }

            override fun equals(other: Any?): Boolean = (other as? OrMatcher)?.let { o ->
                (o.first == first && o.second == second) ||
                    (o.second == first && o.first == second)
            } ?: false
        }

        infix fun EventMatcher.or(other: EventMatcher): EventMatcher = OrMatcher(this, other)
    }
}

/**
 * Much like [Focusable] except that focus is managed by [Selection] then
 * [onFire] is triggered as needed.
 */
interface Selectable {
    fun onFire(navigation: Navigation): FocusResult
    var selected: Boolean

    /**
     * Called before any interactions with selected/onFire in case the receiver wants it.
     */
    fun setSelectionManager(manager: SelectionManager) {
    }
}

/**
 * Determines the behavior when the next button is hit at the end
 * of the list, or the previous button at the beginning.
 */
enum class SelectionMode {
    /**
     * When the end of the list is reached in either direction, clear focus.
     */
    CLEAR_FOCUS,

    /**
     * Do nothing, just stay at the first or last item.
     */
    CAP_ENDS,

    /**
     * Loop around to the beginning/end of the list.
     */
    LOOP
}

class SelectionManager(
    private val navigation: Navigation,
    private var selectKey: EventMatcher,
    private var nextKey: EventMatcher? = null,
    private var lastKey: EventMatcher? = null,
    private val selectionMode: SelectionMode = SelectionMode.CLEAR_FOCUS
) : Focusable {
    var selectables = listOf<Selectable>()
        set(value) {
            field = value
            if (selection != null && !field.contains(selection)) {
                selection = null
            }
        }

    override var focused: Boolean = false
        set(value) {
            selection?.selected = false
            field = value
            if (focused) selection?.select()
        }
    var selection: Selectable? = null
        set(value) {
            if (focused) field?.selected = false
            field = value
            if (focused) field?.select()
        }

    private fun Selectable.select() {
        setSelectionManager(this@SelectionManager)
        selected = true
    }

    override fun onInput(keyStroke: KeyStroke): FocusResult {
        return when {
            selectKey.matches(keyStroke) -> fire()
            nextKey?.matches(keyStroke) == true -> ConsumeEvent.also { next() }
            lastKey?.matches(keyStroke) == true -> ConsumeEvent.also { last() }
            else -> Unhandled
        }
    }

    fun inputFromSelectable(keyStroke: KeyStroke) = onInput(keyStroke).let {
        if (it == ConsumeEvent) ChangeFocus(this)
        else it
    }

    fun next() {
        if (selection == null) {
            selection = selectables.firstOrNull()
        } else {
            val index = selectables.indexOf(selection) + 1
            if (index < selectables.size) {
                selection = selectables[index]
            } else {
                when (selectionMode) {
                    SelectionMode.CLEAR_FOCUS -> {
                        selection = null
                    }
                    SelectionMode.CAP_ENDS -> {
                        // Do nothing
                    }
                    SelectionMode.LOOP -> {
                        selection = selectables.first()
                    }
                }
            }
        }
    }

    fun last() {
        if (selection == null) {
            selection = selectables.lastOrNull()
        } else {
            val index = selectables.indexOf(selection) - 1
            if (index >= 0) {
                selection = selectables[index]
            } else {
                when (selectionMode) {
                    SelectionMode.CLEAR_FOCUS -> {
                        selection = null
                    }
                    SelectionMode.CAP_ENDS -> {
                        // Do nothing
                    }
                    SelectionMode.LOOP -> {
                        selection = selectables.last()
                    }
                }
            }
        }
    }

    private fun fire(): FocusResult {
        selection?.setSelectionManager(this)
        return selection?.onFire(navigation) ?: ConsumeEvent
    }
}
