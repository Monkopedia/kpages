package com.monkopedia.kpages.preferences

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.input.KeyType
import com.monkopedia.dynamiclayout.CachingPanel
import com.monkopedia.dynamiclayout.DynamicHorizontalLinearLayout
import com.monkopedia.dynamiclayout.DynamicVerticalLinearLayout
import com.monkopedia.dynamiclayout.Fill
import com.monkopedia.dynamiclayout.SizeSpec
import com.monkopedia.dynamiclayout.WeightedLayoutParams
import com.monkopedia.dynamiclayout.Wrap
import com.monkopedia.kpages.Navigator
import com.monkopedia.lanterna.ComponentHolder
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.EventMatcher
import com.monkopedia.lanterna.EventMatcher.Companion.matcher
import com.monkopedia.lanterna.EventMatcher.Companion.or
import com.monkopedia.lanterna.FocusResult
import com.monkopedia.lanterna.LinearPanelHolder
import com.monkopedia.lanterna.Selectable
import com.monkopedia.lanterna.SelectionManager
import com.monkopedia.lanterna.WindowHolder
import com.monkopedia.lanterna.border
import com.monkopedia.lanterna.horizontal
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.lanterna.on
import com.monkopedia.lanterna.space
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.spannable.Spanned
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class JvmPreferenceScreen(private val navigator: Navigator, private val root: PreferenceBuilder) :
    Screen("JvmPreferenceScreen") {

    override fun ComponentHolder.createWindow() {
        vertical {
            border {
                label("Preference screen")
            }
            addComponent(root)
        }
    }

    private val allSelectables = findSelectables(root).also {
        for (c in it) {
            c.screen = this
        }
    }
    private val selectionManager by lazy {
        SelectionManager(
            navigation,
            EventMatcher.keyType(KeyType.Enter),
            EventMatcher.keyType(KeyType.ArrowDown),
            EventMatcher.keyType(KeyType.ArrowUp)
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

private interface PreferenceView : Selectable {
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
        SelectionPreferenceImpl<T>()

    fun createSwitchPreferenceProps(): SwitchPreferenceProps = SwitchPreferenceImpl()

    fun createSwitchPreferenceCategoryProps(): SwitchPreferenceCategoryProps =
        SwitchPreferenceCategoryImpl()
}

private class SwitchPreferenceImpl : CachingPanel(), SwitchPreferenceProps, PreferenceView {
    var titleLabel: SpannableLabel
    var subtitleLabel: SpannableLabel
    var checkbox: SpannableLabel

    init {
        val linearLayout = DynamicHorizontalLinearLayout(this)
        layoutManager = linearLayout
        LinearPanelHolder(this, linearLayout).apply {
            vertical {
                titleLabel = label("")
                subtitleLabel = label("")
            }
            space(0).layoutParams(Wrap, Wrap, weight = 1)
            checkbox = label("[ ]")
        }
    }

    private var state: Boolean = false
    override var initialState: Boolean? = false
        set(value) {
            field = value
            state = value!!
        }
    override var onChange: (suspend (Boolean) -> Unit)? = null
        set(value) {
            field = value
            screen?.updateSelectables()
        }

    override var screen: JvmPreferenceScreen? = null
    override val selectable: Boolean
        get() = onClick != null || onChange != null

    override fun onFire(navigation: Navigation): FocusResult {
        state = !state
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
        subtitleLabel.layoutData = WeightedLayoutParams(Fill, if (subtitle != null) Wrap else SizeSpec.specify(0))
        subtitleLabel.setText(Spanned().apply {
            subtitle?.let {
                append(it, EnableSGRSpan(*(if (selected) arrayOf(SGR.REVERSE) else arrayOf())))
            }
        })
        checkbox.text = if (state) "[X]" else "[ ]"
    }
}

private class SelectionPreferenceImpl<T : SelectionOption> : CachingPanel(),
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
        subtitleLabel.layoutData = WeightedLayoutParams(Fill, if (subtitle != null) Wrap else SizeSpec.specify(0))
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

private class PreferenceImpl : CachingPanel(), PreferenceProps, PreferenceView {
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
        subtitleLabel.layoutData = WeightedLayoutParams(Fill, if (subtitle != null) Wrap else SizeSpec.specify(0))
        subtitleLabel.setText(Spanned().apply {
            append(
                subtitle ?: "",
                EnableSGRSpan(*(if (selected) arrayOf(SGR.REVERSE) else arrayOf()))
            )
        })
    }
}


private class PreferenceCategoryImpl : CachingPanel(), PreferenceCategoryProps, PreferenceView {
    override var screen: JvmPreferenceScreen?? = null
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

private class SwitchPreferenceCategoryImpl : CachingPanel(), SwitchPreferenceCategoryProps,
    PreferenceView {
    override var screen: JvmPreferenceScreen?? = null
    override val builder: PreferenceBuilder = PreferenceBuilder()
    override val selectable: Boolean
        get() = onChange != null

    private var state: Boolean = false
    override var initialState: Boolean? = false
        set(value) {
            field = value
            state = value!!
        }
    override var onChange: (suspend (Boolean) -> Unit)? = null
        set(value) {
            field = value
            screen?.updateSelectables()
        }
    var titleLabel: SpannableLabel
    var checkbox: SpannableLabel

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
                builder.layoutData = WeightedLayoutParams(SizeSpec.specify(0), Wrap, weight = 1)
                addComponent(builder)
                space(1)
            }
        }
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
        titleLabel.setText(Spanned().apply {
            append(title ?: "", if (selected) EnableSGRSpan(SGR.BOLD, SGR.REVERSE) else EnableSGRSpan(SGR.BOLD))
        })
        checkbox.text = if (state) "[X]" else "[ ]"
    }
}
