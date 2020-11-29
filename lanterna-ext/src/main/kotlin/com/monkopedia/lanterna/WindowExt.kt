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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.TextGUIThread
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.monkopedia.lanterna.Lanterna.screen
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.util.exceptionHandler
import com.monkopedia.util.logger
import java.io.EOFException
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

open class MaxWidthWindow : BasicWindow() {
    var fullscreen = false

    override fun getPreferredSize(): TerminalSize {
        if (fullscreen) {
            return screen.terminalSize.withRelative(-4, -3)
        }
        val preferredSize = super.getPreferredSize()
        if (preferredSize.columns > screen.terminalSize.columns - 4) {
            return TerminalSize(screen.terminalSize.columns - 4, preferredSize.rows).also {
                contentHolder.preferredSize = it
            }
        }
        return preferredSize
    }
}

private val LOGGER = CoroutineWindow::class.logger
open class CoroutineWindow : MaxWidthWindow() {
    private val job = SupervisorJob()
    val scope = CoroutineScope(job + Dispatchers.GUI + LOGGER.exceptionHandler)

    override fun close() {
        super.close()
        job.cancel()
    }

    override fun setTextGUI(textGUI: WindowBasedTextGUI?) {
        super.setTextGUI(textGUI)
        if (textGUI == null) {
            job.cancel()
        }
    }
}

open class ObservableWindow : CoroutineWindow() {

    interface Observer {
        fun onAttach() {}
        fun onDetach() {}
    }

    private val observers = mutableListOf<Observer>()

    fun addObserver(observer: Observer) {
        scope.launch(Dispatchers.GUI) {
            observers.add(observer)
            if (textGUI != null) {
                observer.onAttach()
            }
        }
    }

    fun removeObserver(observer: Observer) {
        scope.launch(Dispatchers.GUI) {
            observers.remove(observer)
            if (textGUI != null) {
                observer.onDetach()
            }
        }
    }

    override fun setTextGUI(textGUI: WindowBasedTextGUI?) {
        super.setTextGUI(textGUI)
        if (textGUI != null) {
            observers.forEach(Observer::onAttach)
        } else {
            observers.forEach(Observer::onDetach)
        }
    }
}

open class ScreenWindow(val screen: Screen) : ObservableWindow()

val Component.screenWindow: ScreenWindow?
    get() = (basePane as? ScreenWindow)

val Component.screen: Screen?
    get() = screenWindow?.screen

val Component.navigation: Navigation?
    get() = screenWindow?.screen?.navigation

inline fun ObservableWindow.onAttach(crossinline method: () -> Unit) {
    addObserver(object : ObservableWindow.Observer {
        override fun onAttach() {
            method.invoke()
        }
    })
}

inline fun ObservableWindow.onDetach(crossinline method: () -> Unit) {
    addObserver(object : ObservableWindow.Observer {
        override fun onDetach() {
            method.invoke()
        }
    })
}

fun runGuiThread(forever: Boolean = false) {
    var sleep = true
    while (Lanterna.activeWindows.isNotEmpty() || !sleep || forever) {
        Lanterna.activeWindows.removeIf {
            it.textGUI == null
        }
        sleep = true
        val guiThread: TextGUIThread = Lanterna.gui.guiThread
        if (Thread.currentThread() === guiThread.thread) {
            sleep = try {
                !guiThread.processEventsAndUpdate()
            } catch (_: EOFException) {
                // The GUI has closed so allow exit
                break
            } catch (e: IOException) {
                throw RuntimeException(
                    "Unexpected IOException while waiting for window to close",
                    e
                )
            }
        }
        if (sleep) {
            try {
                Thread.sleep(1)
            } catch (ignore: InterruptedException) {
            }
        }
    }
}
