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

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ListViewHolder implements ListAdapter.OnLoadMoreListener {
    View mRootView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mListRv;
    ListAdapter mAdapter;
    OnListChangeListener mListener;

    private boolean isRefreshing = false;
    private boolean isLoading = false;
    boolean hasMore = true;

    public ListViewHolder(LayoutInflater inflater, @Nullable ViewGroup container, RecyclerView.LayoutManager manager) {
        mRootView = inflater.inflate(R.layout.layout_recycler, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dispatchRefresh();
            }
        });

        mListRv = (RecyclerView) mRootView.findViewById(R.id.list_rv);
        mListRv.setLayoutManager(manager);
        mAdapter = new ListAdapter(new ListAdapter.ListAdapterCallback() {
            @Override
            public boolean isListRefreshing() {
                return mSwipeRefreshLayout.isRefreshing();
            }
        });
        mAdapter.setOnLoadMoreListener(this);
        //mAdapter.setFooterProvider(new FooterModel.Provider(Integer.MAX_VALUE, new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        dispatchLoadMore();
        //    }
        //}), new FooterState(FooterState.STATE_LOADING));
        mListRv.setAdapter(mAdapter);
    }

    public void onAttach() {
        dispatchRefresh();
    }

    public View getRootView() {
        return mRootView;
    }

    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    public RecyclerView getRecyclerView() {
        return mListRv;
    }

    public void dispatchRefresh() {
        if (isRefreshing || isLoading) {
            return;
        }

        isRefreshing = true;
        isLoading = false;
        if (mListener != null) {
            mListener.onListRefresh();
        }
    }

    public void dispatchLoadMore() {
        mListRv.post(new Runnable() {
            @Override
            public void run() {
                if (hasMore) {
                    mAdapter.showFooter(FooterState.STATE_LOADING);
                    if (mListener != null) {
                        mListener.onListLoadMore();
                    }
                } else {
                    mAdapter.showFooter(FooterState.STATE_NO_MORE);
                }
            }
        });
    }

    @Override
    public void onListLoadMore() {
        dispatchLoadMore();
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public void resetViewState() {
        isRefreshing = false;
        isLoading = false;
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public OnListChangeListener getOnListChangeListener() {
        return mListener;
    }

    public void setOnListChangeListener(OnListChangeListener listener) {
        mListener = listener;
    }

    public interface OnListChangeListener {
        void onListRefresh();

        void onListLoadMore();
    }
}
