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

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.monkopedia.util.exceptionHandler
import com.monkopedia.util.logger
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.Executors
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val LOGGER = Navigation::class.logger
class Navigation(val gui: MultiWindowTextGUI) {

    private val executor = Executors.newSingleThreadExecutor { r -> Thread(r, "Navigation") }
    private val dispatcher = executor.asCoroutineDispatcher() + LOGGER.exceptionHandler
    private val scope = CoroutineScope(dispatcher)

    private val backStack = mutableListOf<Screen>()
    private var topScreen: Screen? = null
        set(value) {
            LOGGER.info("Navigation: setTopScreen $value $field")
            if (field == value) return
            val lastWindow = field
            field = value
            scope.launch {
                LOGGER.info("Navigation: Hiding $lastWindow, showing $value")
                lastWindow?.hide(this@Navigation)
                value?.show(this@Navigation)
            }
        }

    suspend fun open(screen: Screen) {
        withContext(dispatcher) {
            LOGGER.info("Navigation: open $screen")
            topScreen?.let { backStack.add(it) }
            topScreen = screen
        }
    }

    suspend fun replace(screen: Screen) {
        withContext(dispatcher) {
            try {
                LOGGER.info("Navigation: replace $topScreen with $screen")
                topScreen?.destroy(this@Navigation)
                topScreen = screen
            } catch (t: Throwable) {
                LOGGER.info(
                    StringWriter().also {
                        t.printStackTrace(PrintWriter(it))
                    }.toString()
                )
                LOGGER.info(
                    StringWriter().also {
                        t.cause?.printStackTrace(PrintWriter(it))
                    }.toString()
                )
                Thread.sleep(1000)
                exitProcess(1)
            }
        }
    }

    suspend fun popScreen() {
        withContext(dispatcher) {
            topScreen?.destroy(this@Navigation)
            topScreen = backStack.removeLastOrNull().also {
                LOGGER.info("Navigation: open $it")
            }
        }
    }

    suspend fun destroy() {
        executor.shutdown()
        backStack.forEach {
            it.destroy(this)
        }
        topScreen?.destroy(this)
    }
}