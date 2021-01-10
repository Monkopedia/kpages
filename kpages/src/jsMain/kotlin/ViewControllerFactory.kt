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
import react.rClass
import react.ReactElement
import kotlin.reflect.KClass

inline fun factory(noinline f: RBuilder.(Mutable<CharSequence>) -> ReactElement?): ViewControllerFactory {
    return object : ViewControllerFactory() {
        override val componentFactory: RBuilder.(Mutable<CharSequence>) -> ReactElement? = f
    }
}

actual abstract class ViewControllerFactory {
    abstract val componentFactory: RBuilder.(Mutable<CharSequence>) -> ReactElement?
}


inline fun <P: RProps, T : RComponent<P, *>> ClassFactory(cls: KClass<T>, noinline r: RHandler<out RProps> = {}) =
    factory { cls.rClass.invoke(r).also { child(it) } }
//open class ClassFactory<T : RComponent<*, *>>(private val cls: KClass<T>) :
//    ViewControllerFactory() {
//    override val componentFactory: RBuilder.() -> ReactElement? = {
//        println("Class factory")
//        child(cls.js.newInstance()) {}
//    }
//}

fun <T : Any> JsClass<T>.newInstance(): T {
    inline fun callCtor(ctor: dynamic) = js("new ctor()")
    return callCtor(asDynamic()) as T
}


actual class Navigator {
    actual val path: String
        get() = window.location.pathname

    actual suspend fun goBack() {
        window.history.back()
    }

    actual suspend fun push(path: String) {
        window.open(path, "_self")
    }

    companion object {
        val INSTANCE = Navigator()
    }
}
