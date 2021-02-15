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

val PreferenceDemoScreen = PreferenceScreen("Preference demo") { nav ->
    preferenceCategory("First category") {
        preference {
            title = "First item"
        }
        preference {
            title = "Second item"
            subtitle = "With a subtitle"
        }
        preference {
            title = "Clickable item"
            subtitle = "With a subtitle"
            onClick = {
                println("Common code click")
                nav.push("/preference_demo_2")
            }
        }
        switchPreference {
            title = "A Switch"
            initialState = false
            onChange = {
                println("A Switch: $it")
            }
        }
    }
    switchPreferenceCategory("Second category", true, onChange = { println("Pref category $it") }) {
        switchPreference {
            title = "On switch"
            initialState = true
            onChange = {
                println("On Switch: $it")
            }
        }
        data class BasicSelect(override var label: String) : SelectionOption
        selectionPreference<BasicSelect> {
            title = "My selection"
            initialState = BasicSelect("Second")
            option(BasicSelect("First"))
            option(BasicSelect("Second"))
            option(BasicSelect("Third"))
            onChange = {
                println("Selection: $it")
            }
        }
    }
}

val PreferenceDemoScreen_2 = PreferenceScreen {
    object : PreferenceAdapter() {

        private var isOpen = false

        override val title: String
            get() = "Preference screen 2!"

        override fun PreferenceBuilder.build() {
            preferenceCategory("Second screen") {
                switchPreference {
                    title = "Extra options"
                    initialState = isOpen
                    onChange = {
                        isOpen = it
                        notifyChanged()
                    }
                }
                if (isOpen) {
                    preference {
                        title = "Another back"
                        onClick = {
                            navigator.goBack()
                        }
                    }
                }
                preference {
                    title = "Go back"
                    onClick = {
                        navigator.goBack()
                    }
                }
            }
        }
    }
}