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
import com.ccfraser.muirwik.components.mThemeProvider
import com.monkopedia.kpages.KPagesComponent
import com.monkopedia.kpages.LifecycleComponent
import com.monkopedia.kpages.LifecycleHolder
import com.monkopedia.kpages.Mutable
import com.monkopedia.kpages.demo.DemoApp
import com.monkopedia.kpages.demo.theme
import com.monkopedia.kpages.onValue
import kotlinx.browser.document
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.height
import kotlinx.css.marginTop
import kotlinx.css.overflowY
import kotlinx.css.px
import kotlinx.css.vh
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.h1
import react.dom.render
import react.setState
import styled.css
import styled.styledDiv

fun main() {

    render(document.getElementById("root")) {
        val title = Mutable("Loading" as CharSequence)
        mThemeProvider(theme = theme) {
            styledDiv {
                css {
                    marginTop = LinearDimension("70px")
                    height = 100.vh - 70.px
                    overflowY = Overflow.auto
                }
                child(TitleBar::class) {
                    attrs {
                        this.title = title
                    }
                }
                child(KPagesComponent::class) {
                    attrs {
                        app = DemoApp()
                        this.title = title
                    }
                }
            }
        }
    }
}

external interface TitleProps: RProps {
    var title: Mutable<CharSequence>?
}
external interface TitleState: RState {
    var title: CharSequence?
}

class TitleBar : LifecycleComponent<TitleProps, TitleState>() {
    private val holder = lifecycleHolder()
    init {
        props?.title?.intoState {
            this.title = it
        }
    }
    override fun RBuilder.render() {
        holder.lifecycle = props.title?.toState {
            this.title = it
        }
        h1 {
            +(state.title ?: "").toString()
        }
    }

}
