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
package com.monkopedia.kpages.preferences

import com.monkopedia.kpages.Mutable
import com.monkopedia.kpages.Navigator
import com.monkopedia.kpages.ViewControllerFactory
import com.monkopedia.lanterna.navigation.Screen

actual interface PreferenceBaseProps {
    actual var onClick: (suspend (Any) -> Unit)?
}

actual interface PreferenceProps : PreferenceBaseProps {
    actual var title: String?
    actual var subtitle: String?
}

actual class PreferenceScreen actual constructor(
    private val adapter: (String) -> PreferenceAdapter
) : ViewControllerFactory() {
    override fun create(navigator: Navigator, path: String, title: Mutable<CharSequence>): Screen {
        return JvmPreferenceScreen(navigator, title, adapter(path))
    }
}

actual inline fun PreferenceBuilder.preference(noinline handler: PreferenceProps.() -> Unit) {
    add(createPreferenceProps().also(handler))
}

actual interface PreferenceCategoryProps {
    actual var title: String?
    val builder: PreferenceBuilder
}

actual inline fun PreferenceBuilder.preferenceCategory(
    crossinline handler: PreferenceCategoryProps.() -> Unit,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    val props = createCategory()
    props.handler()
    props.builder.builder()
    add(props)
}

actual inline fun PreferenceBuilder.preferenceCategory(
    title: String,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    val props = createCategory()
    props.title = title
    props.builder.builder()
    add(props)
}

actual interface SelectionOption {
    actual var label: String
}

actual interface SelectionPreferenceProps<T : SelectionOption> : PreferenceProps {
    actual var initialState: T?
    actual var onChange: (suspend (T?) -> Unit)?
    actual var options: List<T>?
}


actual inline fun <reified T : SelectionOption> PreferenceBuilder.selectionPreference(
    noinline handler: SelectionPreferenceProps<T>.() -> Unit
) {
    add(createSelectionPreferenceProps<T>().also(handler))
}

actual interface SwitchPreferenceProps : PreferenceProps {
    actual var initialState: Boolean?
    actual var onChange: (suspend (Boolean) -> Unit)?
}


actual inline fun PreferenceBuilder.switchPreference(
    noinline handler: SwitchPreferenceProps.() -> Unit
) {
    add(createSwitchPreferenceProps().also(handler))
}

actual interface SwitchPreferenceCategoryProps : PreferenceCategoryProps {
    actual var initialState: Boolean?
    actual var onChange: (suspend (Boolean) -> Unit)?
}

actual inline fun PreferenceBuilder.switchPreferenceCategory(
    noinline handler: SwitchPreferenceCategoryProps.() -> Unit,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    add(createSwitchPreferenceCategoryProps().apply {
        handler()
        this.builder.builder()
    })
}

actual inline fun PreferenceBuilder.switchPreferenceCategory(
    title: String,
    initialState: Boolean,
    noinline onChange: (suspend (Boolean) -> Unit)?,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
    add(createSwitchPreferenceCategoryProps().apply {
        this.title = title
        this.initialState = initialState
        this.onChange = onChange
        this.builder.builder()
    })
}

actual interface TextInputPreferenceProps : PreferenceProps {
    actual var value: String?
    actual var dialogTitle: String?
    actual var dialogDescription: String?
    actual var hintText: String?
    actual var onChange: (suspend (String) -> Unit)?
}

actual inline fun PreferenceBuilder.textInputPreference(
    noinline handler: TextInputPreferenceProps.() -> Unit,
) {
    add(createTextInputPreferenceProps().also(handler))
}

actual abstract class PreferenceAdapter actual constructor() {
    internal lateinit var navigatorImpl: Navigator
    internal lateinit var onChange: () -> Unit
    actual val navigator: Navigator
        get() = navigatorImpl

    actual fun notifyChanged() {
        onChange()
    }

    actual abstract fun PreferenceBuilder.build()
    actual abstract val title: String
}