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

import com.monkopedia.kpages.Navigator
import com.monkopedia.kpages.ViewControllerFactory

expect class PreferenceScreen(title: String, preferenceBuilder: PreferenceBuilder.(Navigator) -> Unit) :
    ViewControllerFactory

expect interface PreferenceBaseProps {
    var onClick: (suspend (Any) -> Unit)?
}

expect interface PreferenceProps : PreferenceBaseProps {
    var title: String?
    var subtitle: String?
}

expect inline fun PreferenceBuilder.preference(noinline handler: PreferenceProps.() -> Unit = {})

expect interface PreferenceCategoryProps {
    var title: String?
}

expect inline fun PreferenceBuilder.preferenceCategory(
    crossinline handler: PreferenceCategoryProps.() -> Unit = {},
    crossinline builder: PreferenceBuilder.() -> Unit
)

expect inline fun PreferenceBuilder.preferenceCategory(
    title: String,
    crossinline builder: PreferenceBuilder.() -> Unit
)

expect class PreferenceBuilder

expect interface SelectionOption {
    var label: String
}

expect interface SelectionPreferenceProps<T : SelectionOption> : PreferenceProps {
    var initialState: T?
    var onChange: (suspend (T?) -> Unit)?
    var options: List<T>?
}

inline fun <T : SelectionOption> SelectionPreferenceProps<T>.option(t: T) {
    options = (options ?: emptyList()) + t
}

//expect interface SelectionPreferenceState<T : SelectionOption> {
//    var selection: T?
//}

expect inline fun <reified T : SelectionOption> PreferenceBuilder.selectionPreference(
    noinline handler: SelectionPreferenceProps<T>.() -> Unit = {}
)
expect interface SwitchPreferenceProps : PreferenceProps {
    var initialState: Boolean?
    var onChange: (suspend (Boolean) -> Unit)?
}

//expect interface SwitchPreferenceState {
//    var selected: Boolean?
//}

expect inline fun PreferenceBuilder.switchPreference(
    noinline handler: SwitchPreferenceProps.() -> Unit = {}
)

expect interface SwitchPreferenceCategoryProps : PreferenceCategoryProps {
    var initialState: Boolean?
    var onChange: (suspend (Boolean) -> Unit)?
}

//expect interface SwitchPreferenceCategoryState {
//    var selected: Boolean?
//}

expect inline fun PreferenceBuilder.switchPreferenceCategory(
    noinline handler: SwitchPreferenceCategoryProps.() -> Unit = {},
    crossinline builder: PreferenceBuilder.() -> Unit
)

expect inline fun PreferenceBuilder.switchPreferenceCategory(
    title: String,
    initialState: Boolean = false,
    noinline onChange: (suspend (Boolean) -> Unit)? = null,
    crossinline builder: PreferenceBuilder.() -> Unit
)
