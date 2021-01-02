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
package com.monkopedia.kpages

import com.monkopedia.lanterna.navigation.Navigation
import com.monkopedia.lanterna.navigation.Screen

inline fun ViewControllerFactory(crossinline factory: (Navigation) -> Screen) =
    object : ViewControllerFactory() {
        override fun create(navigation: Navigation): Screen = factory(navigation)
    }

actual abstract class ViewControllerFactory {
    abstract fun create(navigation: Navigation): Screen
}
actual typealias Navigator = Navigation
