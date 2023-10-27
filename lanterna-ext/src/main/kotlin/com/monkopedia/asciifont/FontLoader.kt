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

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.io.File
import java.util.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json: Json by lazy {
    Json { isLenient = true }
}

private inline fun <reified T : Any> File.readJson(): T =
    json.decodeFromString(if (exists()) readText() else "{}")

private inline fun <reified T : Any> File.writeJson(obj: T) =
    writeText(json.encodeToString(obj))

class FontLoader(private val homeDir: File, private val fontConfig: Properties) {
    private var state: FontState = File(homeDir, "font_state.json").readJson()
        set(value) {
            field = value
            GlobalScope.launch {
                File(homeDir, "font_state.json").writeJson(value)
            }
        }
    private val client = HttpClient()
    private val fontCache = mutableMapOf<String, Font>()
    private val downloadDir = File(homeDir, "fonts").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }

    suspend fun get(font: String): Font {
        return withContext(Dispatchers.IO) {
            synchronized(this) { fontCache[font] } ?: loadFont(font).also {
                synchronized(this) {
                    fontCache[font] = it
                }
            }
        }
    }

    private suspend fun loadFont(font: String): Font {
        val fontFile = File(downloadDir, "$font.flf")
        val url = fontConfig.getProperty(font)
            ?: throw IllegalArgumentException("Font $font isn't configured")
        if (!fontFile.exists() || state.fontCache[font] != url) {
            val response = client.get(url)
            if (!response.status.isSuccess()) {
                throw IllegalStateException(response.toString())
            }
            response.bodyAsChannel().copyAndClose(fontFile.writeChannel())
            state = state.copy(
                fontCache = state.fontCache.toMutableMap().also {
                    it[font] = url
                }
            )
        }
        val fontLines = fontFile.readLines().filter { it.endsWith("@") }
        val size = fontLines.indexOfFirst { it.endsWith("@@") } + 1
        val chars = fontLines.chunked(size).toMutableList()
        val sizing = chars.first()
        return Font(sizing.size, CHARS.zip(chars.map(::FontChar)).toMap())
    }

    companion object {
        private val CHARS = arrayOf(
            ' ', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            ':', ';', '<', '=', '>', '?', '@',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '[', '\\', ']', '^', '_', '`',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '{', '|', '}', '~',
        )
    }
}

@Serializable
data class FontState(
    val fontCache: Map<String, String> = mapOf()
)
