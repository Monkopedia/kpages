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
package com.monkopedia.lanterna.spannable

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.ThemeStyle
import com.googlecode.lanterna.gui2.TextGUIGraphics
import java.util.EnumSet

sealed class StyleSpan<T : StyleSpan<T, S>, S : Any>(val priority: Int) : Span<T> {
    abstract fun start(graphics: TextGUIGraphics): S
    abstract fun end(graphics: TextGUIGraphics, state: S)

    fun forceStart(graphics: TextGUIGraphics): Any = start(graphics)
    fun forceEnd(graphics: TextGUIGraphics, state: Any) = end(graphics, state as S)

    companion object {
        inline fun TextGUIGraphics.withSpans(spans: List<Span<*>>, code: () -> Unit) {
            val styleSpans = spans.filterIsInstance<StyleSpan<*, *>>()
                .sortedBy { it.priority }.map {
                    it to it.forceStart(this)
                }
            code()
            styleSpans.reversed().forEach {
                it.first.forceEnd(this, it.second)
            }
        }
    }
}

private data class ThemeStyleImpl(
    private val foreground: TextColor,
    private val background: TextColor,
    private val sgrs: EnumSet<SGR>
) : ThemeStyle {
    override fun getForeground(): TextColor = foreground
    override fun getBackground(): TextColor = background
    override fun getSGRs(): EnumSet<SGR> = sgrs
}

class ThemeSpan(override val style: ThemeStyle) : ThemeSelector<ThemeSpan>(1) {
    override fun copy() = ThemeSpan(style)
}

abstract class ThemeSelector<T : ThemeSelector<T>>(priority: Int) :
    StyleSpan<T, ThemeStyle>(priority) {
    abstract val style: ThemeStyle
    override fun start(graphics: TextGUIGraphics): ThemeStyle {
        val oldStyle = ThemeStyleImpl(
            graphics.foregroundColor,
            graphics.backgroundColor,
            graphics.activeModifiers
        )
        graphics.applyThemeStyle(style)
        graphics.enableModifiers(*oldStyle.sgRs.toTypedArray())
        return oldStyle
    }

    override fun end(graphics: TextGUIGraphics, state: ThemeStyle) {
        graphics.applyThemeStyle(state)
    }
}

class ForegroundSpan(val color: TextColor) : StyleSpan<ForegroundSpan, TextColor>(2) {
    override fun start(graphics: TextGUIGraphics): TextColor {
        return graphics.foregroundColor.also {
            graphics.foregroundColor = color
        }
    }

    override fun end(graphics: TextGUIGraphics, state: TextColor) {
        graphics.foregroundColor = state
    }

    override fun copy() = ForegroundSpan(color)
}

class BackgroundSpan(val color: TextColor) : StyleSpan<BackgroundSpan, TextColor>(3) {
    override fun start(graphics: TextGUIGraphics): TextColor {
        return graphics.backgroundColor.also {
            graphics.backgroundColor = color
        }
    }

    override fun end(graphics: TextGUIGraphics, state: TextColor) {
        graphics.backgroundColor = state
    }

    override fun copy() = BackgroundSpan(color)
}

class EnableSGRSpan(vararg val modifier: SGR) : StyleSpan<EnableSGRSpan, EnumSet<SGR>>(4) {
    override fun start(graphics: TextGUIGraphics): EnumSet<SGR> {
        return graphics.activeModifiers.also {
            graphics.enableModifiers(*modifier)
        }
    }

    override fun end(graphics: TextGUIGraphics, state: EnumSet<SGR>) {
        graphics.setModifiers(state)
    }

    override fun copy() = EnableSGRSpan(*modifier)
}

class DisableSGRSpan(vararg val modifier: SGR) : StyleSpan<DisableSGRSpan, EnumSet<SGR>>(4) {
    override fun start(graphics: TextGUIGraphics): EnumSet<SGR> {
        return graphics.activeModifiers.also {
            graphics.disableModifiers(*modifier)
        }
    }

    override fun end(graphics: TextGUIGraphics, state: EnumSet<SGR>) {
        graphics.setModifiers(state)
    }

    override fun copy() = DisableSGRSpan(*modifier)
}
