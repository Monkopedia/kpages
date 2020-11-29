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
package com.monkopedia.kpages.demo

import com.monkopedia.kpages.KPagesApp
import com.monkopedia.kpages.RouteBuilder
import com.monkopedia.kpages.preferences.PreferenceDemoScreen

class DemoApp : KPagesApp() {

    override fun RouteBuilder.routes() {
        prefixRoute("/theme_demo", ThemeDemoFactory)
        route("/preference_demo", PreferenceDemoScreen)
        prefixRoute("/", RootDemoFactory)
    }
}
