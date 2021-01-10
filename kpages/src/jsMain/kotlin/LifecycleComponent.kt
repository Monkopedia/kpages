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

import kotlinext.js.js
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import react.RComponent
import react.RProps
import react.RState
import react.setState

abstract class LifecycleComponent<P : RProps, S : RState> : RComponent<P, S> {

    private val callbacks = mutableListOf<LifecycleAware>()

    constructor() : super()
    constructor(props: P) : super(props)

    fun lifecycleHolder() = LifecycleHolder().also(::registerLifecycleAware)

    fun registerLifecycleAware(l: LifecycleAware) {
        l.onActivate?.invoke()
        callbacks.add(l)
    }

    inline fun attach(builder: LifecycleAware.() -> Unit) {
        registerLifecycleAware(LifecycleAware().apply(builder))
    }

    fun <T> Mutable<T>.intoState(s: S.(T) -> Unit) {
        registerLifecycleAware(toState(s))
    }

    fun <T> Mutable<T>.toState(s: S.(T) -> Unit) = onValue { v ->
        // Avoid being in rendering.
        GlobalScope.launch {
            delay(1)
            setState {
                s(v)
            }
        }
    }

    override fun componentWillUnmount() {
        callbacks.forEach {
            it.onDeactivate?.invoke()
        }
    }
}

class LifecycleHolder(lifecycle: LifecycleAware? = null) : LifecycleAware {
    var lifecycle: LifecycleAware? = lifecycle
        set(value) {
            if (field != value) {
                if (isActive) {
                    field?.onDeactivate?.invoke()
                }
                field = value
                if (isActive) {
                    field?.onActivate?.invoke()
                }
            }
        }
    private var isActive = false

    override var onActivate: (() -> Unit)?
        get() = {
            if (!isActive) {
                isActive = true
                lifecycle?.onActivate?.invoke()
            }
        }
        set(value) {}
    override var onDeactivate: (() -> Unit)?
        get() = {
            if (isActive) {
                isActive = false
                lifecycle?.onDeactivate?.invoke()
            }
        }
        set(value) {}
}

fun LifecycleAware() = js { }.unsafeCast<LifecycleAware>()
external interface LifecycleAware {
    var onActivate: (() -> Unit)?
    var onDeactivate: (() -> Unit)?
}

fun <T> Mutable<T>.onValue(callback: (T) -> Unit): LifecycleAware {
    return LifecycleAware().apply {
        onActivate = {
            attach(callback)
        }
        onDeactivate = {
            detach(callback)
        }
    }
}