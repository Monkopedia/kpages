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

import com.googlecode.lanterna.graphics.ThemeStyle
import com.monkopedia.lanterna.ConsumeEvent
import com.monkopedia.lanterna.FocusResult
import com.monkopedia.lanterna.Selectable
import com.monkopedia.lanterna.navigation.Navigation

fun interface Action {
    fun invoke(navigation: Navigation)
}

class LinkSpan(
    val link: Action,
    private val selectedTheme: ThemeStyle,
    private val unselectedTheme: ThemeStyle
) : ThemeSelector<LinkSpan>(6), Selectable {

    override var selected: Boolean = false

    override val style: ThemeStyle
        get() = if (selected) selectedTheme else unselectedTheme

    override fun onFire(navigation: Navigation): FocusResult {
        link.invoke(navigation)
        return ConsumeEvent
    }

    override fun copy() = LinkSpan(link, selectedTheme, unselectedTheme)
}
