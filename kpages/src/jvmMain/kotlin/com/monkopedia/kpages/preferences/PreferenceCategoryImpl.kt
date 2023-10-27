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
import com.monkopedia.dynamiclayout.Fill
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

internal class PreferenceCategoryImpl : CachingPanel(), PreferenceCategoryProps, PreferenceView {
    override var screen: JvmPreferenceScreen? = null
    override var onClick: (suspend (Any) -> Unit)? = null
    override val selectable: Boolean
        get() = false
    private var prefParent: CachingPanel
    var titleLabel: SpannableLabel

    init {
        val linearLayout = DynamicVerticalLinearLayout(this)
        layoutManager = linearLayout
        LinearPanelHolder(this, linearLayout).apply {
            border {
                titleLabel = label("").layoutParams(Fill, Wrap)
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
        return ConsumeEvent
    }

    override var selected: Boolean = false
    override var title: String? = null
        set(value) {
            field = value
            rebind()
        }

    private fun rebind() {
        titleLabel.setText(
            Spanned().apply {
                append(title ?: "", EnableSGRSpan(SGR.BOLD))
            }
        )
    }
}
