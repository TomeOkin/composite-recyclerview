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
import android.view.View;

public interface ModelFactory<T extends Item> {
    @LayoutRes int getLayout();

    int getSpanCount(int totalSpanCount);

    ViewModel<T> onCreateViewHolder(View itemView);

    boolean areItemsTheSame(T item1, T item2);

    boolean areContentsTheSame(T oldItem, T newItem);

    /**
     * the order of models
     */
    int modelOrder();

    int compare(T o1, T o2);
}
