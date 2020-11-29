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

import com.monkopedia.kpages.ViewControllerFactory

actual interface PreferenceBaseProps {
    actual var onClick: ((Any) -> Unit)?
}

actual interface PreferenceProps : PreferenceBaseProps {
    actual var title: String?
    actual var subtitle: String?
}

actual class PreferenceScreen actual constructor(
    title: String,
    preferenceBuilder: PreferenceBuilder.() -> Unit
) : ViewControllerFactory()
actual inline fun PreferenceBuilder.preference(noinline handler: PreferenceProps.() -> Unit) {
}

actual interface PreferenceCategoryProps {
    actual var title: String?
    actual var children: ((PreferenceBuilder) -> Unit)?
}

actual inline fun PreferenceBuilder.preferenceCategory(
    crossinline handler: PreferenceCategoryProps.() -> Unit,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
}

actual inline fun PreferenceBuilder.preferenceCategory(
    title: String,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
}

actual class PreferenceBuilder

actual interface SelectionOption {
    actual var label: String
}

actual interface SelectionPreferenceProps<T : SelectionOption> : PreferenceProps {
    actual var initialState: T?
    actual var onChange: ((T?) -> Unit)?
    actual var options: List<T>?
}

actual interface SelectionPreferenceState<T : SelectionOption> {
    actual var selection: T?
}

actual inline fun <reified T : SelectionOption> PreferenceBuilder.selectionPreference(
    noinline handler: SelectionPreferenceProps<T>.() -> Unit
) {}

actual interface SwitchPreferenceProps : PreferenceProps {
    actual var initialState: Boolean?
    actual var onChange: ((Boolean) -> Unit)?
}

actual interface SwitchPreferenceState {
    actual var selected: Boolean?
}

actual inline fun PreferenceBuilder.switchPreference(
    noinline handler: SwitchPreferenceProps.() -> Unit
) {}

actual interface SwitchPreferenceCategoryProps : PreferenceCategoryProps {
    actual var initialState: Boolean?
    actual var onChange: ((Boolean) -> Unit)?
}

actual interface SwitchPreferenceCategoryState {
    actual var selected: Boolean?
}

actual inline fun PreferenceBuilder.switchPreferenceCategory(
    noinline handler: SwitchPreferenceCategoryProps.() -> Unit,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
}

actual inline fun PreferenceBuilder.switchPreferenceCategory(
    title: String,
    initialState: Boolean,
    noinline onChange: ((Boolean) -> Unit)?,
    crossinline builder: PreferenceBuilder.() -> Unit
) {
}
