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

import com.monkopedia.kpages.INSTANCE
import com.monkopedia.kpages.JsControllerFactory
import com.monkopedia.kpages.KPageProps
import com.monkopedia.kpages.Navigator
import com.monkopedia.kpages.ViewControllerFactory
import emotion.react.css
import react.ChildrenBuilder
import react.FC
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useState
import web.cssom.px

actual fun PreferenceScreen(adapter: (String) -> PreferenceAdapter): ViewControllerFactory =
    JsPreferenceScreen(adapter)

class JsPreferenceScreen(
    private val adapter: (String) -> PreferenceAdapter
) : JsControllerFactory() {
    override val element: FC<KPageProps> = FC { props ->
        var preferences: PreferenceAdapter? by useState()
        var state by useState(0)
        useEffect(props.route) {
            preferences = adapter(props.route ?: "")
        }
        div {
            css {
                paddingLeft = 16.px
                paddingRight = 16.px
            }
            preferences?.apply {
                navigator = Navigator.INSTANCE
                onChange = {
                    props.title?.value = title
                    state += 1
                }
                PreferenceBuilder(this@div).build()
            }
        }
    }
}

actual class PreferenceBuilder(val base: ChildrenBuilder)

actual abstract class PreferenceAdapter actual constructor() : PreferenceAdapterBase() {
    actual fun notifyChanged() {
        onChange()
    }

    actual abstract fun PreferenceBuilder.build()
    actual abstract val title: String
}
