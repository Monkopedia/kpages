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
package com.monkopedia.dynamiclayout

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.Panel

open class CachingPanel : Panel() {

    private var workingFillColorOverride: TextColor? = null
    private var componentCache: List<Component>? = null
    private var dynamicChildrenCache: List<DynamicLayout>? = null
    val dynamicChildren: List<DynamicLayout>
        get() = dynamicChildrenCache ?: childrenList.map(Component::asDynamicLayout).also {
            dynamicChildrenCache = it
        }

    override fun addComponent(index: Int, component: Component): Panel {
        return super.addComponent(index, component).also {
            componentCache = null
            dynamicChildrenCache = null
        }
    }

    override fun removeComponent(component: Component?): Boolean {
        return super.removeComponent(component).also {
            componentCache = null
            dynamicChildrenCache = null
        }
    }

    override fun getChildren(): Collection<Component> {
        return childrenList
    }

    override fun getChildrenList(): List<Component> {
        return componentCache ?: super.getChildrenList().also {
            componentCache = it.toList()
        }
    }

    override fun getFillColorOverride(): TextColor? {
        return workingFillColorOverride
    }

    /**
     * Sets an override color to be used instead of the theme's color for Panels when drawing unused space. If called
     * with `null`, it will reset back to the theme's color.
     * @param fillColor Color to draw the unused space with instead of what the theme definition says, no `null`
     * to go back to the theme definition
     */
    override fun setFillColorOverride(fillColor: TextColor?) {
        workingFillColorOverride = fillColor
    }
}
