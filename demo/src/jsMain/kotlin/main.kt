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
import com.monkopedia.konstructor.frontend.utils.useCollected
import com.monkopedia.kpages.KPagesComponent
import com.monkopedia.kpages.Mutable
import com.monkopedia.kpages.demo.DemoApp
import emotion.react.css
import kotlinx.browser.document
import mui.material.styles.ThemeProvider
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import web.cssom.Auto
import web.cssom.minus
import web.cssom.px
import web.cssom.vh
import web.dom.Element

fun main() {
    val root = createRoot(document.getElementById("root")!!.unsafeCast<Element>())
    val title = Mutable("Loading" as CharSequence)
    root.render(
        ThemeProvider.create {
            this.theme = com.monkopedia.kpages.demo.theme
            div {
                css {
                    marginTop = 70.px
                    height = 100.vh - 70.px
                    overflowY = Auto.auto
                }
                TitleBar {
                    this.title = title
                }
                KPagesComponent {
                    app = DemoApp()
                    this.title = title
                }
            }
        }
    )
}

external interface TitleProps : Props {
    var title: Mutable<CharSequence>?
}

val TitleBar = FC<TitleProps> { props ->
    val title = props.title?.useCollected("")
    h1 {
        +(title ?: "").toString()
    }
}
