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
package com.monkopedia.kpages.preferences

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.input.KeyType
import com.monkopedia.dynamiclayout.CachingPanel
import com.monkopedia.dynamiclayout.DynamicVerticalLinearLayout
import com.monkopedia.kpages.Mutable
import com.monkopedia.kpages.Navigator
import com.monkopedia.lanterna.ComponentHolder
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.EventMatcher
import com.monkopedia.lanterna.EventMatcher.Companion.and
import com.monkopedia.lanterna.EventMatcher.Companion.matcher
import com.monkopedia.lanterna.EventMatcher.Companion.or
import com.monkopedia.lanterna.Selectable
import com.monkopedia.lanterna.SelectionManager
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.lanterna.on
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.launch

class JvmPreferenceScreen(
    private val navigator: Navigator,
    private val title: Mutable<CharSequence>,
    private val adapter: PreferenceAdapter
) : Screen("JvmPreferenceScreen") {

    private lateinit var parent: Panel
    private var allSelectables = emptyList<PreferenceView>()
    private var root: Component? = null

    init {
        adapter.navigator = navigator
        adapter.onChange = {
            title.value = adapter.title
            root?.let {
                parent.removeComponent(it)
            }
            root = with(adapter) {
                PreferenceBuilder().also {
                    it.build()
                }
            }.also { root ->
                parent.addComponent(root)
                allSelectables = findSelectables(root).also {
                    for (c in it) {
                        c.screen = this
                    }
                }
                updateSelectables()
            }
        }
    }

    override fun ComponentHolder.createWindow() {
        parent = vertical {
        }
        adapter.onChange()
    }

    private val selectionManager by lazy {
        SelectionManager(
            navigation,
            KeyType.Enter.matcher() or ' '.matcher(),
            KeyType.ArrowDown.matcher() or KeyType.ArrowRight.matcher() or KeyType.Tab.matcher(),
            KeyType.ArrowUp.matcher() or
                KeyType.ArrowLeft.matcher() or
                (KeyType.Tab.matcher() and EventMatcher.Companion.ShiftDown)
        )
    }

    override suspend fun onCreate() {
        super.onCreate()
        focusManager.defaultHandler = selectionManager
        focusManager.keymap.create("Back") {
            launch {
                navigator.goBack()
            }
            ConsumeEvent
        } on (KeyType.Escape.matcher() or KeyType.Backspace.matcher())
        updateSelectables()
    }

    internal fun updateSelectables() {
        selectionManager.selectables = allSelectables.filter { it.selectable }
    }
}

private fun findSelectables(root: Panel): List<PreferenceView> {
    return listOfNotNull(root as? PreferenceView) + root.children.flatMap {
        (it as? Panel)?.let { findSelectables(it) } ?: listOfNotNull(it as? PreferenceView)
    }
}

internal interface PreferenceView : Selectable {
    var screen: JvmPreferenceScreen?
    val selectable: Boolean
}

actual class PreferenceBuilder : CachingPanel() {
    init {
        layoutManager = DynamicVerticalLinearLayout(this)
    }

    fun add(also: PreferenceBaseProps) {
        addComponent(also as Component)
        addComponent(EmptySpace(TerminalSize(1, 1)))
    }

    fun add(also: PreferenceCategoryProps) {
        addComponent(also as Component)
        addComponent(EmptySpace(TerminalSize(1, 1)))
    }

    fun createPreferenceProps(): PreferenceProps = PreferenceImpl()

    fun createCategory(): PreferenceCategoryProps = PreferenceCategoryImpl()

    fun <T : SelectionOption> createSelectionPreferenceProps(): SelectionPreferenceProps<T> =
        SelectionPreferenceImpl()

    fun createSwitchPreferenceProps(): SwitchPreferenceProps = SwitchPreferenceImpl()

    fun createSwitchPreferenceCategoryProps(): SwitchPreferenceCategoryProps =
        SwitchPreferenceCategoryImpl()

    fun createTextInputPreferenceProps(): TextInputPreferenceProps = TextInputPreferenceImpl()
}
