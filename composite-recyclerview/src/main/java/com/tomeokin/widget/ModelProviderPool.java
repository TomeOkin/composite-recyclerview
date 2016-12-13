/*
 * Copyright 2016 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomeokin.widget;

import android.support.annotation.LayoutRes;
import android.support.v4.util.ArrayMap;

public class ModelProviderPool {
    private ArrayMap<Class<? extends Item>, ModelFactory> mGroup = new ArrayMap<>(1);

    public void register(Class<? extends Item> item, ModelFactory factory) {
        mGroup.put(item, factory);
    }

    public ModelFactory getProvider(Class<? extends Item> item) {
        return mGroup.get(item);
    }

    public ModelFactory getProvider(@LayoutRes int layout) {
        for (ModelFactory factory : mGroup.values()) {
            if (factory.getLayout() == layout) {
                return factory;
            }
        }
        return null;
    }
}
