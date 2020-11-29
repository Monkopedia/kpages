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

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowListener
import com.googlecode.lanterna.input.KeyStroke
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.util.logger
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.launch

sealed class FocusResult

object ConsumeEvent : FocusResult()
object ReleaseFocus : FocusResult()
object NavigateBack : FocusResult()
object Unhandled : FocusResult()
data class ChangeFocus(val newTarget: Focusable) : FocusResult()

interface Focusable {
    fun onInput(keyStroke: KeyStroke): FocusResult

    var focused: Boolean
}

val Component.focusManager: FocusManager
    get() = FocusManager.of(screenWindow?.screen ?: error("Not attached to window"))

fun <T> T.takeFocus() where T : Focusable, T : Component {
    focusManager.focus = this
}

class SharedFocus(private vararg val items: Focusable) : Focusable {
    override fun onInput(keyStroke: KeyStroke): FocusResult {
        return items.asSequence().map { it.onInput(keyStroke) }
            .firstOrNull { it != Unhandled } ?: Unhandled
    }

    override var focused: Boolean = false
        set(value) {
            field = value
            items.forEach { it.focused = value }
        }
}
private val LOGGER = FocusManager::class.logger
class FocusManager private constructor(private val screen: Screen) {

    private val windowListener = object : WindowListener {
        override fun onInput(
            basePane: Window,
            keyStroke: KeyStroke,
            deliverEvent: AtomicBoolean
        ) = handleInput(keyStroke).also { deliverEvent.set(false) }

        override fun onUnhandledInput(
            basePane: Window,
            keyStroke: KeyStroke,
            hasBeenHandled: AtomicBoolean
        ) = handleInput(keyStroke).also { hasBeenHandled.set(true) }

        override fun onResized(window: Window, oldSize: TerminalSize?, newSize: TerminalSize) {
        }

        override fun onMoved(
            window: Window,
            oldPosition: TerminalPosition?,
            newPosition: TerminalPosition
        ) {
        }
    }

    private fun handleInput(keyStroke: KeyStroke) {
        val target = listOfNotNull(focus, defaultHandler, keymap)
        for (t in target) {
            if (t.handleInput(keyStroke)) {
                LOGGER.info("Input $keyStroke consumed by $t")
                return
            }
        }
    }

    private fun Focusable.handleInput(keyStroke: KeyStroke): Boolean {
        return when (val result = onInput(keyStroke)) {
            Unhandled -> false
            ConsumeEvent -> {
                // Event handled, nothing to do here.
                true
            }
            ReleaseFocus -> {
                if (focus != null) {
                    focus = null
                } else {
                    error("Default handler cannot release focus")
                }
                true
            }
            NavigateBack -> {
                screen.launch {
                    screen.navigation.popScreen()
                }
                true
            }
            is ChangeFocus -> {
                focus = result.newTarget
                true
            }
        }
    }

    var focus: Focusable? = null
        set(value) {
            (field ?: defaultHandler)?.focused = false
            field = value
            (field ?: defaultHandler)?.focused = true
        }

    /**
     * Receives focus when no focus is received.
     */
    var defaultHandler: Focusable? = null
        set(value) {
            if (focus == null) field?.focused = false
            field = value
            if (focus == null) field?.focused = true
        }

    val keymap = Keymap()

    init {
        screen.window.onAttach {
            activeManagers[screen] = this
            screen.window.addWindowListener(windowListener)
        }
        screen.window.onDetach {
            screen.window.removeWindowListener(windowListener)
            activeManagers.remove(screen)
        }
    }

    companion object {
        private val activeManagers = mutableMapOf<Screen, FocusManager>()
        fun of(screen: Screen): FocusManager {
            return activeManagers.getOrPut(screen) {
                FocusManager(screen)
            }
        }
    }
}
