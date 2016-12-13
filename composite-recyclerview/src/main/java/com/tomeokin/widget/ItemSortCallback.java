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

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;

class ItemSortCallback extends SortedList.Callback<Item> {
    private ListAdapter mAdapter;
    private ModelProviderPool mProviderPool;

    ItemSortCallback(@NonNull ListAdapter adapter, @NonNull ModelProviderPool providerPool) {
        mAdapter = adapter;
        mProviderPool = providerPool;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Item o1, Item o2) {
        if (o1.getClass().equals(o2.getClass())) {
            return mProviderPool.getProvider(o1.getClass()).compare(o1, o2);
        } else {
            return mProviderPool.getProvider(o1.getClass()).modelOrder() - mProviderPool.getProvider(o2.getClass())
                .modelOrder();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean areItemsTheSame(Item item1, Item item2) {
        ModelFactory factory = mProviderPool.getProvider(item1.getClass());
        return factory.areItemsTheSame(item1, item2);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean areContentsTheSame(Item oldItem, Item newItem) {
        ModelFactory factory = mProviderPool.getProvider(oldItem.getClass());
        return factory.areContentsTheSame(oldItem, newItem);
    }

    @Override
    public void onChanged(int position, int count) {
        mAdapter.notifyItemChanged(position, count);
    }

    @Override
    public void onInserted(int position, int count) {
        mAdapter.notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
        mAdapter.notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        mAdapter.notifyItemMoved(fromPosition, toPosition);
    }
}
