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
package com.monkopedia.dynamiclayout

import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.LayoutData
import kotlin.math.min
import kotlin.reflect.KClass

enum class MeasureType {
    AT_MOST,
    EXACTLY,
    UNSPECIFIED
}

data class MeasureSpec(val type: MeasureType, val size: Int) {

    inline fun size(desired: Int): Int {
        return when (type) {
            MeasureType.AT_MOST -> min(size, desired)
            MeasureType.EXACTLY -> size
            MeasureType.UNSPECIFIED -> desired
        }
    }

    operator fun plus(change: Int): MeasureSpec {
        return MeasureSpec(type, (size + change).coerceAtLeast(0))
    }

    operator fun minus(change: Int): MeasureSpec {
        return MeasureSpec(type, (size - change).coerceAtLeast(0))
    }

    companion object {
        fun exactly(size: Int): MeasureSpec = MeasureSpec(MeasureType.EXACTLY, size)
        fun atMost(size: Int): MeasureSpec = MeasureSpec(MeasureType.AT_MOST, size)
        fun unspecified(size: Int): MeasureSpec = MeasureSpec(MeasureType.UNSPECIFIED, size)
    }
}

sealed class SizeSpec {
    abstract fun forChild(spec: MeasureSpec): MeasureSpec

    companion object {
        fun specify(size: Int) = SpecifiedSize(size)
    }
}

data class SpecifiedSize(val size: Int) : SizeSpec() {
    override fun forChild(spec: MeasureSpec): MeasureSpec = when (spec.type) {
        MeasureType.AT_MOST -> MeasureSpec(MeasureType.EXACTLY, min(size, spec.size))
        MeasureType.EXACTLY -> MeasureSpec(MeasureType.EXACTLY, min(size, spec.size))
        MeasureType.UNSPECIFIED -> MeasureSpec(MeasureType.EXACTLY, size)
    }

    override fun toString(): String = size.toString()
}

object Wrap : SizeSpec() {
    override fun forChild(spec: MeasureSpec): MeasureSpec = MeasureSpec(
        if (spec.type == MeasureType.UNSPECIFIED) MeasureType.UNSPECIFIED
        else MeasureType.AT_MOST,
        spec.size
    )

    override fun toString(): String = "Wrap"
}

object Fill : SizeSpec() {
    override fun forChild(spec: MeasureSpec): MeasureSpec = spec

    override fun toString(): String = "Fill"
}

open class LayoutParams(val cols: SizeSpec, val rows: SizeSpec) : LayoutData {
    abstract class LayoutCompanion<T : LayoutParams>(private val cls: KClass<T>) {
        fun coerce(params: LayoutParams?): T {
            if (cls.isInstance(params)) {
                return params as T
            }
            return params?.let { convert(params) } ?: createDefault()
        }

        abstract fun convert(params: LayoutParams): T
        abstract fun createDefault(): T

        fun coerce(component: Component) {
            component.layoutData = coerce(component.asDynamicLayout.layoutParams)
        }
    }

    companion object : LayoutCompanion<LayoutParams>(LayoutParams::class) {
        override fun createDefault(): LayoutParams = LayoutParams(Wrap, Wrap)
        override fun convert(params: LayoutParams): LayoutParams = params
    }

    override fun toString(): String {
        return "LayoutParams($cols, $rows)"
    }
}
