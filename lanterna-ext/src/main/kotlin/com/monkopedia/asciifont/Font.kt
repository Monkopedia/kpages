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
package com.monkopedia.asciifont

class FontChar(rawRows: List<String>) {
    val rows = rawRows.map {
        when {
            it.endsWith("@@") -> it.substring(
                0,
                it.length - 2
            )
            it.endsWith("@") -> it.substring(0, it.length - 1)
            else -> error("Missing end character")
        }.replace("$", " ")
    }
    val width: Int = rows.first().length

    init {
        require(rows.all { it.length == width }) {
            "Invalid character size"
        }
    }
}

data class Font(val height: Int, val characters: Map<Char, FontChar>) {
    fun measureWidth(text: CharSequence): Int {
        return text.map { get(it) }.sumBy { it.width }
    }

    operator fun get(c: Char): FontChar {
        return characters[c] ?: (characters[' '] ?: error("Missing default char ' '"))
    }
}
