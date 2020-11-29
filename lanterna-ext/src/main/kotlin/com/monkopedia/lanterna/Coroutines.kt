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
package com.monkopedia.lanterna

import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.Label
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext

val Dispatchers.GUI: CoroutineDispatcher get() = Lanterna.guiDispatcher

val Component.coroutineWindow: CoroutineWindow?
    get() = (basePane as? CoroutineWindow)

// TODO: Don't fall back to global scope
val Component.coroutineScope: CoroutineScope
    get() = coroutineWindow?.scope ?: GlobalScope

suspend inline fun Label.setTextFromBg(newText: String) = withContext(Dispatchers.GUI) {
    text = newText
}
