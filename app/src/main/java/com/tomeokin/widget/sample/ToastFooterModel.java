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
import android.widget.Toast;

import com.tomeokin.widget.FooterState;
import com.tomeokin.widget.ModelFactory;
import com.tomeokin.widget.ViewModel;

public class ToastFooterModel extends ViewModel<FooterState> {
    View loadMoreView;
    View loadMoreFailedView;

    public ToastFooterModel(View itemView, View.OnClickListener listener) {
        super(itemView);
        itemView.setOnClickListener(listener);
        loadMoreView = itemView.findViewById(R.id.load_more_container);
        loadMoreFailedView = itemView.findViewById(R.id.load_more_failed_container);
    }

    @Override
    public void bindView(FooterState item) {
        switch (item.getState()) {
            case FooterState.STATE_LOADING:
                loadMoreView.setVisibility(View.VISIBLE);
                loadMoreFailedView.setVisibility(View.GONE);
                break;
            case FooterState.STATE_ERROR:
                loadMoreView.setVisibility(View.GONE);
                loadMoreFailedView.setVisibility(View.VISIBLE);
                break;
            case FooterState.STATE_NO_MORE:
                loadMoreView.setVisibility(View.GONE);
                loadMoreFailedView.setVisibility(View.GONE);
                Toast.makeText(loadMoreView.getContext(), R.string.no_more_description, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void recycledView() {

    }

    public static final class Provider implements ModelFactory<FooterState> {
        private final int modelOrder;
        private final View.OnClickListener listener;

        public Provider(int modelOrder, View.OnClickListener listener) {
            this.modelOrder = modelOrder;
            this.listener = listener;
        }

        @Override
        public int getLayout() {
            return R.layout.layout_toast_footer;
        }

        @Override
        public ToastFooterModel onCreateViewHolder(View itemView) {
            return new ToastFooterModel(itemView, listener);
        }

        @Override
        public int getSpanCount(int totalSpanCount) {
            return 1;
        }

        // no use for footer
        @Override
        public boolean areItemsTheSame(FooterState item1, FooterState item2) {
            return false;
        }

        // no use for footer
        @Override
        public boolean areContentsTheSame(FooterState oldItem, FooterState newItem) {
            return false;
        }

        @Override
        public int modelOrder() {
            return modelOrder;
        }

        // no use for footer
        @Override
        public int compare(FooterState o1, FooterState o2) {
            return 0;
        }
    }
}
