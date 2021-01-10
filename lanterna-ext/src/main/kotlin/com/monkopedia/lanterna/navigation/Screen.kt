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
package com.monkopedia.lanterna.navigation

import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.input.KeyType
import com.monkopedia.dynamiclayout.CachingPanel
import com.monkopedia.lanterna.Command
import com.monkopedia.lanterna.ComponentHolder
import com.monkopedia.lanterna.EventMatcher
import com.monkopedia.lanterna.EventMatcher.Companion.and
import com.monkopedia.lanterna.EventMatcher.Companion.matcher
import com.monkopedia.lanterna.FocusManager
import com.monkopedia.lanterna.GUI
import com.monkopedia.lanterna.NavigateBack
import com.monkopedia.lanterna.ScreenWindow
import com.monkopedia.lanterna.WindowHolder
import com.monkopedia.lanterna.buildViews
import com.monkopedia.lanterna.frame
import com.monkopedia.lanterna.on
import com.monkopedia.lanterna.onAttach
import com.monkopedia.lanterna.onDetach
import com.monkopedia.lanterna.screenWindow
import com.monkopedia.lanterna.vertical
import com.monkopedia.util.exceptionHandler
import com.monkopedia.util.logger
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

private val LOGGER = Screen::class.logger
abstract class Screen(val name: String) : CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext =
        Dispatchers.GUI + job + LOGGER.exceptionHandler

    private var windowImpl: ScreenWindow? = null
    private var navigationImpl: Navigation? = null

    val window: ScreenWindow
        get() = windowImpl ?: error("Screen has not been created yet")
    val navigation: Navigation
        get() = navigationImpl ?: error("Screen has not been created yet")

    private var isCreated = false
    private var isShowing = false

    val focusManager: FocusManager by lazy {
        FocusManager.of(this).also {
            it.keymap.create("Quit") {
                exitProcess(1)
            } on (EventMatcher.Companion.CtrlDown and 'c'.matcher())
        }
    }
    val backCommand: Command by lazy {
        focusManager.keymap.create("Close and go to last screen") {
            return@create NavigateBack
        }
    }
    private val headerView by lazy {
        buildViews {
            frame {  }
        }.first() as CachingPanel
    }

    abstract fun ComponentHolder.createWindow()

    internal suspend fun create(navigation: Navigation) {
        LOGGER.info("$this: create")
        withContext(coroutineContext) {
            navigationImpl = navigation
            require(!isCreated) {
                "Screen already created"
            }
            windowImpl = doCreateWindow()
            window.onAttach {
                navigation.attachHeader(headerView)
            }
            window.onDetach {
                navigation.detachHeader(headerView)
            }
            isCreated = true
        }
        launch(coroutineContext) {
            LOGGER.info("${this@Screen}: onCreate")
            onCreate()
        }
    }

    open suspend fun doCreateWindow(): ScreenWindow {
        screenWindow(navigation.gui) {
            vertical {
                addComponent(headerView)
                this.createWindow()
            }
            return window as ScreenWindow
        }
    }

    internal suspend fun show(navigation: Navigation) {
        if (!isCreated) {
            create(navigation)
        }
        LOGGER.info("$this: show")
        withContext(coroutineContext) {
            if (isShowing) return@withContext
            navigation.gui.addWindow(window)
            isShowing = true
        }
        launch(coroutineContext) {
            LOGGER.info("${this@Screen}: onShowing")
            onShowing()
        }
    }

    internal suspend fun hide(navigation: Navigation) {
        if (!isShowing) return
        LOGGER.info("$this: hide")
        withContext(coroutineContext) {
            isShowing = false
            navigation.gui.removeWindow(window)
        }
        launch(coroutineContext) {
            LOGGER.info("${this@Screen}: onHiding")
            onHiding()
        }
    }

    internal suspend fun destroy(navigation: Navigation) {
        if (isShowing) {
            hide(navigation)
        }
        LOGGER.info("$this: destroy")
        withContext(coroutineContext) {
            require(isCreated) {
                "Screen not created"
            }
            LOGGER.info("${this@Screen}: onDestroy")
            onDestroy()
            windowImpl = null
            isCreated = false
        }
    }

    open suspend fun onCreate() {}
    open suspend fun onShowing() {}
    open suspend fun onHiding() {}
    open suspend fun onDestroy() {}

    override fun toString(): String {
        return "$name@${hashCode()}"
    }
}

fun Screen.registerEscapeAsBack() {
    backCommand.addKey(KeyType.Escape.matcher())
}
fun Screen.registerBackspaceAsBack() {
    backCommand.addKey(KeyType.Backspace.matcher())
}
