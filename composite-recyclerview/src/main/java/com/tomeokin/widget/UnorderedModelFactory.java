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

public abstract class UnorderedModelFactory<T extends Item> implements ModelFactory<T> {
    @Override
    public boolean areItemsTheSame(T item1, T item2) {
        return false;
    }

    @Override
    public boolean areContentsTheSame(T oldItem, T newItem) {
        return false;
    }

    @Override
    public int compare(T o1, T o2) {
        return 0;
    }
}
