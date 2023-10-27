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

import com.googlecode.lanterna.SGR
import com.monkopedia.dynamiclayout.CachingPanel
import com.monkopedia.dynamiclayout.DynamicVerticalLinearLayout
import com.monkopedia.dynamiclayout.SizeSpec
import com.monkopedia.dynamiclayout.WeightedLayoutParams
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.FocusResult
import com.monkopedia.lanterna.LinearPanelHolder
import com.monkopedia.lanterna.border
import com.monkopedia.lanterna.horizontal
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.space
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.spannable.Spanned
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class SwitchPreferenceCategoryImpl :
    CachingPanel(),
    SwitchPreferenceCategoryProps,
    PreferenceView {
    override var screen: JvmPreferenceScreen? = null
    override val selectable: Boolean
        get() = onChange != null

    private var state: Boolean = false
    override var initialState: Boolean? = false
        set(value) {
            field = value
            state = value!!
        }
    override var onClick: (suspend (Any) -> Unit)? = null
    override var onChange: (suspend (Boolean) -> Unit)? = null
        set(value) {
            field = value
            screen?.updateSelectables()
        }
    var titleLabel: SpannableLabel
    var checkbox: SpannableLabel
    private var prefParent: CachingPanel

    init {
        val linearLayout = DynamicVerticalLinearLayout(this)
        layoutManager = linearLayout
        LinearPanelHolder(this, linearLayout).apply {
            border {
                horizontal {
                    titleLabel = label("")
                    space(0).layoutParams(Wrap, Wrap, weight = 1)
                    checkbox = label("[ ]")
                }
            }
            horizontal {
                space(1)
                prefParent = vertical { }
                prefParent.layoutData = WeightedLayoutParams(SizeSpec.specify(0), Wrap, weight = 1)
                space(1)
            }
        }
    }

    override var children: ((PreferenceBuilder) -> Unit)? = null
        set(value) {
            field = value
            val builder = PreferenceBuilder()
            value?.invoke(builder)
            prefParent.removeAllComponents()
            prefParent.addComponent(builder)
        }

    override fun onFire(navigation: Navigation): FocusResult {
        state = !state
        rebind()
        GlobalScope.launch {
            onChange?.invoke(state)
        }
        return ConsumeEvent
    }

    override var selected: Boolean = false
        set(value) {
            field = value
            rebind()
        }
    override var title: String? = null
        set(value) {
            field = value
            rebind()
        }

    private fun rebind() {
        titleLabel.setText(
            Spanned().apply {
                append(
                    title ?: "",
                    if (selected) EnableSGRSpan(SGR.BOLD, SGR.REVERSE) else EnableSGRSpan(SGR.BOLD)
                )
            }
        )
        checkbox.text = if (state) "[X]" else "[ ]"
    }
}
