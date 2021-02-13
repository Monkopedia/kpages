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

import kotlinx.browser.window
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.ReactElement
import react.rClass
import kotlin.reflect.KClass

inline fun factory(noinline f: RBuilder.(String, Mutable<CharSequence>) -> ReactElement?): ViewControllerFactory {
    return object : ViewControllerFactory() {
        override fun RBuilder.create(path: String, title: Mutable<CharSequence>): ReactElement? = f(path, title)
    }
}

actual abstract class ViewControllerFactory {
    abstract fun RBuilder.create(path: String, title: Mutable<CharSequence>): ReactElement?
}

inline fun <P : RProps, T : RComponent<P, *>> ClassFactory(
    cls: KClass<T>,
    noinline r: RHandler<out RProps> = {}
) = factory { _, _ -> cls.rClass.invoke(r).also { child(it) } }

internal class NavigatorImpl : Navigator {
    override val path: String
        get() = window.location.pathname

    override suspend fun goBack() {
        window.history.back()
    }

    override suspend fun push(path: String) {
        window.open(path, "_self")
    }

    companion object
}

val Navigator.Companion.INSTANCE: Navigator by lazy { NavigatorImpl() }
