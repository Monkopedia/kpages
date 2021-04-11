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

internal class PreferenceCategoryImpl : CachingPanel(), PreferenceCategoryProps, PreferenceView {
    override var screen: JvmPreferenceScreen? = null
    override val builder: PreferenceBuilder = PreferenceBuilder()
    override val selectable: Boolean
        get() = false
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
                builder.layoutData = WeightedLayoutParams(SizeSpec.specify(0), Wrap, weight = 1)
                addComponent(builder)
                space(1)
            }
        }
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
        titleLabel.setText(Spanned().apply {
            append(title ?: "", EnableSGRSpan(SGR.BOLD))
        })
    }
}