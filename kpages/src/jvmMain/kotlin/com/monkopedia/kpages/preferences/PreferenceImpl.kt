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
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.spannable.Spanned
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class PreferenceImpl : CachingPanel(), PreferenceProps, PreferenceView {
    var titleLabel: SpannableLabel
    var subtitleLabel: SpannableLabel

    init {
        val linearLayout = DynamicVerticalLinearLayout(this)
        layoutManager = linearLayout
        LinearPanelHolder(this, linearLayout).apply {
            titleLabel = label("")
            subtitleLabel = label("")
        }
    }

    override var screen: JvmPreferenceScreen? = null
    override val selectable: Boolean
        get() = onClick != null

    override fun onFire(navigation: Navigation): FocusResult {
        GlobalScope.launch {
            onClick?.invoke(navigation)
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

    override var subtitle: String? = null
        set(value) {
            field = value
            rebind()
        }
    override var onClick: (suspend (Any) -> Unit)? = null
        set(value) {
            field = value
            screen?.updateSelectables()
        }

    private fun rebind() {
        titleLabel.setText(Spanned().apply {
            val sgr = (if (selected) arrayOf(SGR.BOLD, SGR.REVERSE) else arrayOf(SGR.BOLD))
            append(title ?: "", EnableSGRSpan(*sgr))
        })
        subtitleLabel.layoutData =
            WeightedLayoutParams(Fill, if (subtitle != null) Wrap else SizeSpec.specify(0))
        subtitleLabel.setText(Spanned().apply {
            append(
                subtitle ?: "",
                EnableSGRSpan(*(if (selected) arrayOf(SGR.REVERSE) else arrayOf()))
            )
        })
    }
}