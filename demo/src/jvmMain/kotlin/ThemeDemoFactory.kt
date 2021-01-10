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
import com.monkopedia.lanterna.FocusResult
import com.monkopedia.lanterna.Selectable
import com.monkopedia.lanterna.SelectionManager
import com.monkopedia.lanterna.WindowHolder
import com.monkopedia.lanterna.border
import com.monkopedia.lanterna.frame
import com.monkopedia.lanterna.label
import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.navigation.Screen
import com.monkopedia.lanterna.spannable.EnableSGRSpan
import com.monkopedia.lanterna.spannable.SpannableLabel
import com.monkopedia.lanterna.spannable.Spanned
import com.monkopedia.lanterna.vertical
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual val ThemeDemoFactory: ViewControllerFactory
    get() = ViewControllerFactory { ThemeDemoScreen() }


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

class RootDemoScreen(private val navigator: Navigator) : Screen("theme_demo") {
    lateinit var buttonLabel: SpannableLabel
    override fun ComponentHolder.createWindow() {
        frame {
            vertical {
                buttonLabel = label("Next screen")
            }.layoutParams(Wrap, Wrap, Gravity.CENTER)
        }.layoutParams(Fill, Fill)
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
                buttonLabel.setText(if (selected) Spanned().apply {
                    append("Next screen", EnableSGRSpan(SGR.REVERSE))
                } else "Next screen")
            }

    }

    override suspend fun onCreate() {
        super.onCreate()
        val selectionManager = SelectionManager(
            navigation,
            EventMatcher.keyType(KeyType.Enter),
            EventMatcher.keyType(KeyType.ArrowDown),
            EventMatcher.keyType(KeyType.ArrowUp)
        )
        focusManager.defaultHandler = selectionManager
        selectionManager.selectables = listOf(button)
    }
}