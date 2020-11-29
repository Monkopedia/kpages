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

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.DefaultWindowManager
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import com.monkopedia.asciifont.FontLoader
import java.io.File
import java.util.Properties
import java.util.concurrent.Executor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher

object Lanterna {

    lateinit var terminal: Terminal
        private set
    lateinit var screen: Screen
        private set
    lateinit var gui: MultiWindowTextGUI
        private set
    lateinit var guiDispatcher: CoroutineDispatcher
        private set
    lateinit var fontLoader: FontLoader
        private set

    val activeWindows = mutableSetOf<Window>()

    fun init(kindexHome: File, theme: ThemeData, fonts: Properties) {
        terminal = DefaultTerminalFactory().createTerminal()
        screen = TerminalScreen(terminal).also {
            it.startScreen()
        }
        gui = MultiWindowTextGUI(
            screen,
            DefaultWindowManager(),
            EmptySpace(TextColor.ANSI.BLACK)
        )
        guiDispatcher = Executor { runnable ->
            if (Thread.currentThread() === gui.guiThread.thread) {
                runnable.run()
            } else {
                gui.guiThread.invokeLater(runnable)
            }
        }.asCoroutineDispatcher()
        gui.theme = ThemeDataImpl(theme)
//        gui.theme = PropertyTheme(File(kindexHome, "theme.properties").properties)
        fontLoader = FontLoader(kindexHome, fonts)
    }
}
