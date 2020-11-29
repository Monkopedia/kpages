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
import com.monkopedia.lanterna.EventMatcher.Companion.or
import com.monkopedia.util.logger

fun interface KeyHandler {
    fun handle(): FocusResult
}

sealed class Command {
    abstract fun addKey(matcher: EventMatcher): Command
    abstract fun clear(): Command
}

private class CommandImpl(
    val keymap: Keymap,
    val description: String,
    val handler: KeyHandler
) : Command() {
    override fun addKey(matcher: EventMatcher): Command = also {
        keymap.registerKey(this, matcher)
    }

    override fun clear(): Command = also {
        keymap.clearKeys(this)
    }
}

infix fun Command.on(matcher: EventMatcher) = addKey(matcher)

private val LOGGER = Keymap::class.logger
class Keymap : Focusable {
    private val bindings = mutableMapOf<CommandImpl, EventMatcher>()
    override var focused: Boolean = false

    fun create(description: String, handler: KeyHandler): Command =
        CommandImpl(this, description, handler)

    fun registerKey(command: Command, matcher: EventMatcher) {
        if (command !is CommandImpl) throw IllegalArgumentException("How..?")
        bindings[command] = bindings[command]?.let { it or matcher }
            ?: matcher
    }

    fun clearKeys(command: Command) {
        bindings.remove(command)
    }

    override fun onInput(keyStroke: KeyStroke): FocusResult {
        LOGGER.info("Input $keyStroke $bindings")
        for ((command, matcher) in bindings) {
            if (matcher.matches(keyStroke)) {
                return command.handler.handle()
            }
        }
        return Unhandled
    }
}
