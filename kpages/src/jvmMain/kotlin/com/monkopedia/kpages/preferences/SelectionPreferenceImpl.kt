package com.monkopedia.kpages.preferences

import com.googlecode.lanterna.SGR
import com.monkopedia.dynamiclayout.CachingPanel
import com.monkopedia.dynamiclayout.DynamicHorizontalLinearLayout
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.SizeSpec
import com.monkopedia.dynamiclayout.WeightedLayoutParams
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.FocusResult
import com.monkopedia.lanterna.LinearPanelHolder
import com.monkopedia.lanterna.border
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.space
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.spannable.Spanned
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class SelectionPreferenceImpl<T : SelectionOption> : CachingPanel(),
    SelectionPreferenceProps<T>, PreferenceView {
    var titleLabel: SpannableLabel
    var subtitleLabel: SpannableLabel
    var selection: SpannableLabel

    var state: T? = null
    override var initialState: T? = null
        set(value) {
            field = value
            state = value
            rebind()
        }
    override var onChange: (suspend (T?) -> Unit)? = null
        set(value) {
            field = value
            screen?.updateSelectables()
        }
    override var options: List<T>? = null
        set(value) {
            field = value
            rebind()
        }

    init {
        val linearLayout = DynamicHorizontalLinearLayout(this)
        layoutManager = linearLayout
        LinearPanelHolder(this, linearLayout).apply {
            vertical {
                titleLabel = label("")
                subtitleLabel = label("")
            }
            space(0).layoutParams(Wrap, Wrap, weight = 1)
            border {
                selection = label("")
            }
        }
    }

    override var screen: JvmPreferenceScreen? = null
    override val selectable: Boolean
        get() = onClick != null || onChange != null

    override fun onFire(navigation: Navigation): FocusResult {
        state = if (state == null || options == null) options?.first()
        else {
            val index = options!!.indexOf(state!!)
            if (index < 0 || index == (options!!.size - 1)) null
            else options!![index + 1]
        }
        rebind()
        GlobalScope.launch {
            onClick?.invoke(navigation)
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
        selection.setText(Spanned().apply {
            append(
                state?.label ?: "",
                EnableSGRSpan(*(if (selected) arrayOf(SGR.BLINK) else arrayOf()))
            )
        })
    }

}