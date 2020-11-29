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

import java.lang.IllegalArgumentException

interface Span<T : Span<T>> {
    fun copy(): T
}

class StaticSpan(val text: CharSequence, val spans: List<Span<*>>) : CharSequence {
    override val length: Int
        get() = text.length

    override fun get(index: Int): Char = text[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return StaticSpan(text.subSequence(startIndex, endIndex), spans)
    }

    override fun toString(): String {
        return text.toString()
    }
}

/**
 * Container for other spans to be appended to one another.
 */
class Spanned(vararg spans: CharSequence) : CharSequence {

    data class SpanItem(val start: Int, val end: Int, val span: Span<*>) {
        fun contains(index: Int): Boolean {
            return index in start until end
        }

        operator fun plus(offset: Int): SpanItem {
            return SpanItem(start + offset, end + offset, span)
        }

        inline operator fun minus(offset: Int) = this + (-offset)
    }

    val children = mutableListOf<CharSequence>()
    val spans = mutableListOf<SpanItem>()

    init {
        for (span in spans) {
            if (span == this) {
                throw IllegalArgumentException("Adding to self")
            }
            children.add(span)
        }
    }

    fun append(text: CharSequence, vararg spans: Span<*>): Spanned {
        val start = length
        val end = length + text.length
        when (text) {
            is Spanned -> {
                children.addAll(text.children)
                this.spans.addAll(text.spans.map { it + start })
            }
            is StaticSpan -> {
                children.add(text.text)
                this.spans.addAll(text.spans.map { SpanItem(start, end, it) })
            }
            else -> {
                children.add(text)
            }
        }
        for (span in spans) {
            this.spans.add(SpanItem(start, end, span))
        }
        return this
    }

    fun applySpans(start: Int = 0, end: Int = length, vararg spans: Span<*>) {
        for (span in spans) {
            this.spans.add(SpanItem(start, end, span))
        }
    }

    fun toStaticSpans(): List<StaticSpan> {
        val indices = (spans.flatMap { listOf(it.start, it.end) } + 0 + length).toSet().sorted()
        val str = toString()
        return indices.zipWithNext().map { (start, end) ->
            StaticSpan(
                str.substring(start, end),
                spans.filter { it.contains(start) }.map { it.span }
            )
        }
    }

    fun getAllSpans(): List<SpanItem> {
        return spans
    }

    override val length: Int
        get() = children.sumBy(CharSequence::length)

    override fun get(index: Int): Char {
        val (child, localIndex) = childContaining(index)
        return child[localIndex]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): Spanned {
        val spanned = Spanned()
        var index = startIndex
        while (index < endIndex) {
            val (child, childIndex) = childContaining(index)
            val subSequence = child.subSequence(
                childIndex,
                (endIndex - index + childIndex).coerceAtMost(child.length)
            )
            index += subSequence.length
            spanned.append(subSequence)
        }
        spanned.spans.addAll(
            spans.filter { (!((endIndex < it.start) || startIndex > it.end)) }
                .map {
                    SpanItem(
                        (it.start - startIndex).coerceAtLeast(0),
                        it.end.coerceAtMost(endIndex) - startIndex,
                        it.span.copy()
                    )
                }
        )
        return spanned
    }

    override fun toString(): String {
        return children.joinToString(separator = "", transform = Any::toString)
    }

    private fun childContaining(index: Int): Pair<CharSequence, Int> {
        var i = index
        var childIndex = 0
        while (childIndex < children.size && i >= children[childIndex].length) {
            i -= children[childIndex++].length
        }
        if (childIndex == children.size) {
            return Pair(children.last(), children.last().length)
        }
        return Pair(children[childIndex], i)
    }

    fun toUpperCase(): CharSequence {
        return Spanned().also {
            it.children.addAll(children.map { it.toString().toUpperCase() })
            it.spans.addAll(spans)
        }
    }
}
