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

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.graphics.ThemeStyle
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.WindowDecorationRenderer
import com.googlecode.lanterna.gui2.WindowPostRenderer
import java.lang.ref.WeakReference
import java.util.EnumSet
import kotlinx.serialization.Serializable

@Serializable
data class ThemeData(
    val default: ThemeDefinitionData,
    val themeOverrides: Map<String, ThemeDefinitionData> = mapOf(),
    val colors: Map<String, String> = mapOf(),
    val postRenderer: String? = null,
    val decorationRenderer: String? = null
)

@Serializable
data class ThemeDefinitionData(
    val normal: ThemeStyleData? = null,
    val prelight: ThemeStyleData? = null,
    val selected: ThemeStyleData? = null,
    val active: ThemeStyleData? = null,
    val insensitive: ThemeStyleData? = null,
    val custom: Map<String, ThemeStyleData> = mapOf(),
    val properties: Map<String, Boolean> = mapOf(),
    val characters: Map<String, Char> = mapOf(),
    val renderers: Map<String, String> = mapOf(),
    val parentTheme: String? = null,
    val colorOverrides: Map<String, String> = mapOf()
)

@Serializable
data class ThemeStyleData(
    val foreground: String? = null,
    val background: String? = null,
    val sgrs: List<SGR>? = null,
    val colorOverrides: Map<String, String> = mapOf()
)

inline fun ThemeData.mutate(
    theme: String,
    mutation: ThemeDefinitionData.() -> ThemeDefinitionData
): ThemeData {
    if (theme == "default") {
        return copy(default = default.mutation())
    } else {
        return copy(
            themeOverrides = themeOverrides.toMutableMap().apply {
                this[theme] = (this[theme] ?: default).mutation()
            }
        )
    }
}

inline fun ThemeDataImpl.copyWithMutation(
    mutation: ThemeData.(ThemeDataImpl) -> ThemeData
): ThemeDataImpl {
    return ThemeDataImpl(data.mutation(this))
}

class ThemeDataImpl(val data: ThemeData) : ColorHolder("root", data.colors), Theme {
    private val themeCache = mutableMapOf<String, WeakReference<ThemeDefinitionImpl>>()

    override fun getDefaultDefinition(): ThemeDefinitionImpl {
        return create(data.default, "default")
    }

    operator fun get(name: String): ThemeDefinitionImpl {
        if (name == "default") {
            return create(data.default, name)
        }
        val themeData = data.themeOverrides[name] ?: data.default
        return create(themeData, name)
    }

    private fun create(
        themeData: ThemeDefinitionData,
        name: String
    ): ThemeDefinitionImpl {
        return themeCache[name]?.get()
            ?: ThemeDefinitionImpl(
                name, themeData,
                themeData.parentTheme?.let {
                    if (data.themeOverrides.containsKey(name) && name == it) {
                        throw IllegalArgumentException("Circular dependency found")
                    }
                    if (!data.themeOverrides.containsKey(name) &&
                        !data.themeOverrides.containsKey(it)
                    ) {
                        throw IllegalArgumentException("Circular dependency found")
                    }
                    get(it)
                } ?: if (name != "default") defaultDefinition else null,
                this
            ).also {
                themeCache[name] = WeakReference(it)
            }
    }

    override fun getDefinition(clazz: Class<*>): ThemeDefinitionImpl {
        return this[clazz.name]
    }

    private val postRenderer: WindowPostRenderer? by lazy {
        instantiate(data.postRenderer)
    }

    override fun getWindowPostRenderer(): WindowPostRenderer? = postRenderer

    private val decorationRenderer: WindowDecorationRenderer? by lazy {
        instantiate(data.decorationRenderer)
    }

    override fun getWindowDecorationRenderer(): WindowDecorationRenderer? = decorationRenderer

    class ThemeDefinitionImpl(
        name: String,
        val data: ThemeDefinitionData,
        val parentTheme: ThemeDefinitionImpl?,
        val theme: ThemeDataImpl
    ) : ColorHolder(name, data.colorOverrides, parentTheme ?: theme), ThemeDefinition {

        private fun resolve(getter: ThemeDefinitionData.() -> ThemeStyleData?): ThemeStyleData {
            return data.getter() ?: parentTheme?.resolve(getter) ?: error("Can't find style")
        }

        private fun createStyle(name: String, getter: ThemeDefinitionData.() -> ThemeStyleData?):
            ThemeStyleImpl = createStyleInner(name, this, getter)
                ?: error("Can't resolve theme $name ${this.name} $data")

        private fun createStyleInner(
            name: String,
            parent: ThemeDefinitionImpl,
            getter: ThemeDefinitionData.() -> ThemeStyleData?
        ): ThemeStyleImpl? {
            val data = data.getter() ?: return parentTheme?.createStyleInner(name, parent, getter)
            return ThemeStyleImpl(
                "$name(${this.name})", data,
                parentTheme?.createStyleInner(name, parent, getter), parent
            )
        }

        private val normalStyle by lazy {
            createStyle("normal", ThemeDefinitionData::normal)
        }

        override fun getNormal(): ThemeStyle = normalStyle

        private val prelightStyle by lazy {
            createStyle("prelight", ThemeDefinitionData::prelight)
        }

        override fun getPreLight(): ThemeStyle = prelightStyle

        private val insensitiveStyle by lazy {
            createStyle("insensitive", ThemeDefinitionData::insensitive)
        }

        override fun getInsensitive(): ThemeStyle = insensitiveStyle

        private val selectedStyle by lazy {
            createStyle("selected", ThemeDefinitionData::selected)
        }

        override fun getSelected(): ThemeStyle = selectedStyle

        private val activeStyle by lazy {
            createStyle("active", ThemeDefinitionData::active)
        }

        override fun getActive(): ThemeStyle = activeStyle

        override fun getCustom(name: String): ThemeStyle = createStyle(name) { custom[name] }

        override fun getCustom(name: String, defaultValue: ThemeStyle): ThemeStyle = try {
            getCustom(name)
        } catch (t: IllegalStateException) {
            defaultValue
        }

        private fun boolean(name: String?): Boolean? =
            data.properties[name] ?: parentTheme?.boolean(name)

        override fun getBooleanProperty(name: String?, defaultValue: Boolean): Boolean =
            boolean(name) ?: defaultValue

        override fun isCursorVisible(): Boolean = getBooleanProperty("isCursorVisible", true)

        private fun character(name: String?): Char? =
            data.characters[name] ?: parentTheme?.character(name)

        override fun getCharacter(name: String?, fallback: Char): Char = character(name) ?: fallback

        override fun <T : Component?> getRenderer(type: Class<T>?): ComponentRenderer<T>? {
            val renderer = data.renderers[type?.name] ?: return parentTheme?.getRenderer(type)
            return instantiate(renderer)
        }
    }

    class ThemeStyleImpl(
        name: String,
        val data: ThemeStyleData,
        private val parentStyle: ThemeStyleImpl?,
        parent: ThemeDefinitionImpl
    ) : ColorHolder(name, data.colorOverrides, parentStyle ?: parent), ThemeStyle {

        val foregroundColor: String
            get() = data.foreground ?: parentStyle?.foregroundColor
                ?: error("Can't resolve foreground")
        val backgroundColor: String
            get() = data.background ?: parentStyle?.backgroundColor
                ?: error("Can't resolve foreground")

        private val foregroundTextColor by lazy { resolveColor(foregroundColor) }
        override fun getForeground(): TextColor = foregroundTextColor

        private val backgroundTextColor by lazy { resolveColor(backgroundColor) }
        override fun getBackground(): TextColor = backgroundTextColor

        private val sgrSet by lazy {
            data.sgrs?.let {
                if (it.isEmpty()) EnumSet.noneOf(SGR::class.java) else EnumSet.copyOf(it)
            } ?: parentStyle?.sgRs
                ?: error("Can't resolve sgrs")
        }

        override fun getSGRs(): EnumSet<SGR> = sgrSet
    }

    companion object {
        private val STYLE_NORMAL = ""
        private val STYLE_PRELIGHT = "PRELIGHT"
        private val STYLE_SELECTED = "SELECTED"
        private val STYLE_ACTIVE = "ACTIVE"
        private val STYLE_INSENSITIVE = "INSENSITIVE"
    }
}

abstract class ColorHolder(
    val name: String,
    val colors: Map<String, String>,
    val parent: ColorHolder? = null
) {

    val hierarchy: String
        get() = "$name -> ${parent?.hierarchy}"

    fun resolve(color: String): String {
        return parent?.resolve(colors[color] ?: color) ?: colors[color] ?: color
    }

    fun resolveColor(color: String): TextColor {
        try {
            return TextColor.Factory.fromString(resolve(color)).also { }
        } catch (t: Throwable) {
            throw IllegalArgumentException("Resolve failure $hierarchy $color ${resolve(color)}", t)
        }
    }
}

private fun <T> instantiate(className: String?): T? {
    if (className == null || className.trim { it <= ' ' }.isEmpty()) {
        return null
    }
    return Class.forName(className).newInstance() as? T
}
