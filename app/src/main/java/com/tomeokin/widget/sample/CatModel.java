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
package com.tomeokin.widget.sample;

import android.view.View;
import android.widget.TextView;

import com.tomeokin.widget.ModelFactory;
import com.tomeokin.widget.ViewModel;

public class CatModel extends ViewModel<Cat> {
    TextView name;

    public CatModel(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.cat_name_tv);
    }

    @Override
    public void bindView(Cat item, int position) {
        name.setText(item.getName());
    }

    @Override
    public void recycledView() {

    }

    public static final class Provider implements ModelFactory<Cat> {
        private final int modelOrder;

        public Provider(int modelOrder) {
            this.modelOrder = modelOrder;
        }

        @Override
        public int getLayout() {
            return R.layout.layout_cat;
        }

        @Override
        public int getSpanCount(int totalSpanCount) {
            return totalSpanCount;
        }

        @Override
        public CatModel onCreateViewHolder(View itemView) {
            return new CatModel(itemView);
        }

        @Override
        public boolean areItemsTheSame(Cat item1, Cat item2) {
            return item1.getId().equals(item2.getId());
        }

        @Override
        public boolean areContentsTheSame(Cat oldItem, Cat newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public int modelOrder() {
            return modelOrder;
        }

        @Override
        public int compare(Cat o1, Cat o2) {
            // ASC
            return o1.getId().compareTo(o2.getId());
        }
    }
}
