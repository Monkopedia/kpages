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
package com.monkopedia.kpages.demo

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.input.KeyType
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.Gravity
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.kpages.Navigator
import com.monkopedia.kpages.ViewControllerFactory
import com.monkopedia.lanterna.ComponentHolder
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.EventMatcher
import com.monkopedia.lanterna.EventMatcher.Companion.and
import com.monkopedia.lanterna.EventMatcher.Companion.matcher
import com.monkopedia.lanterna.EventMatcher.Companion.or
import com.monkopedia.lanterna.FocusResult
import com.monkopedia.lanterna.Selectable
import com.monkopedia.lanterna.SelectionManager
import com.monkopedia.lanterna.border
import com.monkopedia.lanterna.frame
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.lanterna.space
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.spannable.Spanned
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual val ThemeDemoFactory: ViewControllerFactory
    get() = ViewControllerFactory { _, _ -> ThemeDemoScreen() }

actual val RootDemoFactory: ViewControllerFactory
    get() = ViewControllerFactory(::RootDemoScreen)

class ThemeDemoScreen : Screen("theme_demo") {
    override fun ComponentHolder.createWindow() {
        frame {
            border {
                label("No theme implemented")
            }.layoutParams(Wrap, Wrap, Gravity.CENTER)
        }.layoutParams(Fill, Fill)
    }
}

class RootDemoScreen(private val navigator: Navigator, private val path: String) : Screen(
    "theme_demo"
) {
    lateinit var buttonLabel: SpannableLabel
    override fun ComponentHolder.createWindow() {
        frame {
            vertical {
                space(2)
                buttonLabel = label("Next screen")
                space(2)
                label("Path: $path")
            }.layoutParams(Wrap, Wrap, Gravity.CENTER)
        }
    }

    private val button = object : Selectable {
        override fun onFire(navigation: Navigation): FocusResult {
            GlobalScope.launch {
                navigator.push("/preference_demo")
            }
            return ConsumeEvent
        }

        override var selected: Boolean = false
            set(value) {
                field = value
                buttonLabel.setText(
                    if (selected) {
                        Spanned().apply {
                            append("Next screen", EnableSGRSpan(SGR.REVERSE))
                        }
                    } else {
                        "Next screen"
                    }
                )
            }
    }

    override suspend fun onCreate() {
        super.onCreate()
        val selectionManager = SelectionManager(
            navigation,
            KeyType.Enter.matcher() or ' '.matcher(),
            KeyType.ArrowDown.matcher() or KeyType.ArrowRight.matcher() or KeyType.Tab.matcher(),
            KeyType.ArrowUp.matcher() or
                KeyType.ArrowLeft.matcher() or
                (KeyType.Tab.matcher() and EventMatcher.Companion.ShiftDown)
        )
        focusManager.defaultHandler = selectionManager
        selectionManager.selectables = listOf(button)
    }
}
