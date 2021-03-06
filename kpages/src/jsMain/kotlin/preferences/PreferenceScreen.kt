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
import com.monkopedia.kpages.Mutable
import com.monkopedia.kpages.Navigator
import com.monkopedia.kpages.ViewControllerFactory
import kotlinx.css.paddingLeft
import kotlinx.css.paddingRight
import kotlinx.css.px
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.ReactElement
import react.setState
import styled.css
import styled.styledDiv

actual class PreferenceScreen actual constructor(
    private val adapter: (String) -> PreferenceAdapter
) : ViewControllerFactory() {
    override fun RBuilder.create(path: String, title: Mutable<CharSequence>): ReactElement? {
        return child(PreferenceComponent::class) {
            attrs {
                this.title = title
                preferences = adapter(path)
                navigator = Navigator.INSTANCE
            }
        }
    }
}

external interface PreferenceComponentProps : RProps {
    var preferences: PreferenceAdapter?
    var title: Mutable<CharSequence>?
    var navigator: Navigator?
}

class PreferenceComponent(props: PreferenceComponentProps) :
    RComponent<PreferenceComponentProps, RState>(props) {

    init {
        props.preferences?.component = this
        props.title?.value = props.preferences?.title ?: "Preference screen"
    }

    override fun componentWillReceiveProps(nextProps: PreferenceComponentProps) {
        nextProps.preferences?.component = this
        nextProps.title?.value = nextProps.preferences?.title ?: "Preference screen"
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                paddingLeft = 16.px
                paddingRight = 16.px
            }
            props.preferences?.apply {
                PreferenceBuilder(this@render).build()
            }
        }
    }
}

actual class PreferenceBuilder(val base: RBuilder)

actual abstract class PreferenceAdapter actual constructor() {
    internal lateinit var component: PreferenceComponent
    actual val navigator: Navigator
        get() = Navigator.INSTANCE

    actual fun notifyChanged() {
        component.props.title!!.value = title
        component.forceUpdate {
        }
    }

    actual abstract fun PreferenceBuilder.build()
    actual abstract val title: String
}