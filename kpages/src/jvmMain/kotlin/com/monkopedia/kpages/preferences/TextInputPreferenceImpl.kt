package com.monkopedia.kpages.preferences

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.input.KeyType
import com.monkopedia.dynamiclayout.CachingPanel
import com.monkopedia.dynamiclayout.DynamicHorizontalLinearLayout
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.Gravity
import com.monkopedia.dynamiclayout.SizeSpec
import com.monkopedia.dynamiclayout.WeightedLayoutParams
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.lanterna.ComponentHolder
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.EventMatcher
import com.monkopedia.lanterna.EventMatcher.Companion.and
import com.monkopedia.lanterna.EventMatcher.Companion.matcher
import com.monkopedia.lanterna.EventMatcher.Companion.or
import com.monkopedia.lanterna.FocusResult
import com.monkopedia.lanterna.LinearPanelHolder
import com.monkopedia.lanterna.Selectable
import com.monkopedia.lanterna.SelectionManager
import com.monkopedia.lanterna.TextInput
import com.monkopedia.lanterna.border
import com.monkopedia.lanterna.hdiv
import com.monkopedia.lanterna.horizontal
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.lanterna.selectButton
import com.monkopedia.lanterna.space
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.spannable.Spanned
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.launch

internal class TextInputPreferenceImpl : CachingPanel(), TextInputPreferenceProps, PreferenceView {
    var titleLabel: SpannableLabel
    var subtitleLabel: SpannableLabel

    init {
        val linearLayout = DynamicHorizontalLinearLayout(this)
        layoutManager = linearLayout
        LinearPanelHolder(this, linearLayout).apply {
            vertical {
                titleLabel = label("")
                subtitleLabel = label("")
            }
        }
    }

    override var value: String? = null
    override var dialogTitle: String? = null
    override var dialogDescription: String? = null
    override var hintText: String? = null
    override var onChange: (suspend (String) -> Unit)? = null
        set(value) {
            field = value
            screen?.updateSelectables()
        }

    override var screen: JvmPreferenceScreen? = null
    override val selectable: Boolean
        get() = true

    override fun onFire(navigation: Navigation): FocusResult {
        screen!!.launch {
            navigation.showDialog(
                InputDialog(
                    dialogTitle,
                    dialogDescription,
                    hintText,
                    value,
                    onChange
                )
            )
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

    private fun rebind() {
        titleLabel.setText(Spanned().apply {
            title?.let {
                append(
                    it,
                    EnableSGRSpan(
                        *(
                                if (selected) arrayOf(SGR.BOLD, SGR.REVERSE)
                                else arrayOf(SGR.BOLD))
                    )
                )
            }
        })
        subtitleLabel.layoutData =
            WeightedLayoutParams(Fill, if (subtitle != null) Wrap else SizeSpec.specify(0))
        subtitleLabel.setText(Spanned().apply {
            subtitle?.let {
                append(it, EnableSGRSpan(*(if (selected) arrayOf(SGR.REVERSE) else arrayOf())))
            }
        })
    }
}

class InputDialog(
    private val title: String?,
    private val description: String?,
    private val hint: String?,
    private val initialValue: String?,
    private val onChange: (suspend (String) -> Unit)?
) : Screen("Input") {
    private lateinit var okButton: Selectable
    private lateinit var cancelButton: Selectable
    private lateinit var input: TextInput
    override val isCentered: Boolean
        get() = true

    override fun ComponentHolder.createWindow() {
        border {
            vertical {
                title?.let {
                    label(title)
                    hdiv()
                }
                if (description != null) {
                    label(description)
                    space(1)
                }
                input = TextInput().also {
                    it.hint = hint
                    it.text = initialValue
                    it.layoutData = WeightedLayoutParams(Fill, Wrap)
                    addComponent(it)
                }
                horizontal {
                    space(0).layoutParams(Wrap, Wrap, Gravity.TOP_LEFT, 1)

                    cancelButton = selectButton("Cancel") {
                        launch {
                            navigation.hideDialog(this@InputDialog)
                        }
                    }
                    space(1)
                    okButton = selectButton("OK") {
                        launch {
                            onChange?.invoke(input.text?.toString() ?: "")
                            navigation.hideDialog(this@InputDialog)
                        }
                    }
                }
            }
        }
    }

    override suspend fun onCreate() {
        super.onCreate()
        focusManager.defaultHandler = SelectionManager(
            navigation,
            KeyType.Enter.matcher() or ' '.matcher(),
            KeyType.ArrowDown.matcher() or KeyType.ArrowRight.matcher() or KeyType.Tab.matcher(),
            KeyType.ArrowUp.matcher() or KeyType.ArrowLeft.matcher() or (KeyType.Tab.matcher() and EventMatcher.Companion.ShiftDown)
        ).also {
            it.selectables = listOf(input.asSelectable, cancelButton, okButton)
        }
    }

}


