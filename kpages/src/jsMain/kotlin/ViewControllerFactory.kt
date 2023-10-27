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
import react.ChildrenBuilder
import react.FC
import react.Props

external interface KPageProps : Props {
    var route: String?
    var title: Mutable<CharSequence>?
}

inline fun factory(noinline f: ChildrenBuilder.(KPageProps) -> Unit): ViewControllerFactory {
    return object : JsControllerFactory() {
        override val element: FC<KPageProps> = FC(f)
    }
}

abstract class JsControllerFactory : ViewControllerFactory() {
    abstract val element: FC<KPageProps>
}

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
