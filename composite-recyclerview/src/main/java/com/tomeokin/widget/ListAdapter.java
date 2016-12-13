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
import android.support.v7.util.SortedList;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ViewModel<Item>> {
    private final SortedList.BatchedCallback<Item> mBatchCallback;
    private SortedList<Item> mItemSortList;

    private RecyclerView mRecyclerView;
    private ModelProviderPool mProviderPool = new ModelProviderPool();
    private ModelFactory mFooterProvider;
    private FooterState mFooterItem;
    private ModelFactory mHeaderProvider;
    private Item mHeaderItem;
    private boolean mShowHeader;
    private boolean mShowingFooter;

    private boolean mSpanSizeLookUpEnable;
    int mVisibleRows = 5;
    int mSpanCount = 1;

    private final GridLayoutManager.SpanSizeLookup mSpanSizeLookUp = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            int spanSize = getSpanCount(mSpanCount, position);
            if (spanSize < 1) {
                spanSize = mSpanCount;
            }
            return spanSize;
        }
    };

    public ListAdapter(ListAdapterCallback callback) {
        mCallback = callback;
        mBatchCallback = new SortedList.BatchedCallback<>(new ItemSortCallback(this, mProviderPool));
        mItemSortList = new SortedList<>(Item.class, mBatchCallback);
    }

    public <T extends FooterState> void setFooterProvider(ModelFactory<T> footerProvider, @Nullable T item) {
        mFooterProvider = footerProvider;
        mFooterItem = item != null ? item : new FooterState();
    }

    public <T extends Item> void setHeaderProvider(ModelFactory<T> headerProvider, @Nullable T item) {
        mHeaderProvider = headerProvider;
        mHeaderItem = item;
        mItemSortList.add(item);
        mProviderPool.register(mHeaderItem.getClass(), mHeaderProvider);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ViewModel<Item> onCreateViewHolder(ViewGroup parent, int layout) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        // header and footer
        if (shouldShowHeader() && layout == mHeaderProvider.getLayout()) {
            return mHeaderProvider.onCreateViewHolder(view);
        } else if (shouldShowFooter() && layout == mFooterProvider.getLayout()) {
            return mFooterProvider.onCreateViewHolder(view);
        }

        ModelFactory factory = mProviderPool.getProvider(layout);
        return factory != null ? factory.onCreateViewHolder(view) : null;
    }

    @Override
    public void onBindViewHolder(ViewModel<Item> holder, int position) {
        if (isHeader(position)) {
            holder.bindView(mHeaderItem);
        } else if (isFooter(position)) {
            holder.bindView(mFooterItem);
        } else {
            Item item = mItemSortList.get(position);
            holder.bindView(item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return mHeaderProvider.getLayout();
        } else if (isFooter(position)) {
            return mFooterProvider.getLayout();
        }
        Item item = mItemSortList.get(position - getHeaderSize());
        return mProviderPool.getProvider(item.getClass()).getLayout();
    }

    int getSpanCount(int totalSpanCount, int position) {
        if (isHeader(position)) {
            return mHeaderProvider.getSpanCount(totalSpanCount);
        } else if (isFooter(position)) {
            return mFooterProvider.getSpanCount(totalSpanCount);
        }
        Item item = mItemSortList.get(position - getHeaderSize());
        return mProviderPool.getProvider(item.getClass()).getSpanCount(totalSpanCount);
    }

    @Override
    public void onViewRecycled(ViewModel<Item> holder) {
        holder.recycledView();
    }

    @Override
    public void onViewAttachedToWindow(ViewModel<Item> holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();

        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            if (isHeader(position) || isFooter(position)) {
                p.setFullSpan(true);
            }
        }

        //判断是否需要自动加载
        //Log.i("Adapter", "onViewAttachedToWindow position: " + position);
        //if (mRecyclerView == null || mRecyclerView.getScrollState() != SCROLL_STATE_IDLE) return;
        //if (isFooter(position)) {
        //
        //}
    }

    @Override
    public void onViewDetachedFromWindow(ViewModel<Item> holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = (GridLayoutManager) layoutManager;
            mSpanCount = gridManager.getSpanCount();
            if (shouldSpanSizeLookUp(mSpanCount)) {
                gridManager.setSpanSizeLookup(mSpanSizeLookUp);
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            mSpanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int[] mLastVisibleItemPositions;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (shouldLoadMore(recyclerView.getLayoutManager())) {
                    dispatchLoadMore();
                }
            }

            public boolean shouldLoadMore(RecyclerView.LayoutManager manager) {
                int totalItemCount = manager.getItemCount();
                int lastVisibleItem = findLastVisibleItemPosition(manager);
                int visibleThreshold = mVisibleRows * mSpanCount;
                visibleThreshold = Math.max(visibleThreshold, manager.getChildCount());
                return !isListRefreshing() && !isLoadingMore() && totalItemCount <= lastVisibleItem + visibleThreshold;
            }

            public int findLastVisibleItemPosition(RecyclerView.LayoutManager manager) {
                if (manager instanceof LinearLayoutManager) {
                    return ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                } else if (manager instanceof StaggeredGridLayoutManager) {
                    final StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager) manager;
                    if (mLastVisibleItemPositions == null) {
                        mLastVisibleItemPositions = new int[lm.getSpanCount()];
                    }

                    lm.findLastVisibleItemPositions(mLastVisibleItemPositions);
                    // get maximum element within the list
                    return getLastVisibleItem(mLastVisibleItemPositions);
                }
                return 0;
            }

            public int getLastVisibleItem(int[] lastVisibleItemPositions) {
                int maxSize = 0;
                for (int i = 0; i < lastVisibleItemPositions.length; i++) {
                    if (i == 0) {
                        maxSize = lastVisibleItemPositions[i];
                    } else if (lastVisibleItemPositions[i] > maxSize) {
                        maxSize = lastVisibleItemPositions[i];
                    }
                }
                return maxSize;
            }
        });
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    private boolean isHeader(int position) {
        return position == 0 && shouldShowHeader();
    }

    private boolean isFooter(int position) {
        return position == getHeaderSize() + mItemSortList.size() && shouldShowFooter();
    }

    public boolean isShowingFooter() {
        return shouldShowFooter();
    }

    @Override
    public int getItemCount() {
        return getHeaderSize() + mItemSortList.size() + getFooterSize();
    }

    public int getNormalItemCount() {
        return mItemSortList.size();
    }

    public int getHeaderSize() {
        return shouldShowHeader() ? 1 : 0;
    }

    public int getFooterSize() {
        return shouldShowFooter() ? 1 : 0;
    }

    public void showFooter(int state) {
        if (supportButNotShowingFooter()) {
            mShowingFooter = true;
            mFooterItem.setState(state);
            notifyItemInserted(getHeaderSize() + mItemSortList.size() - 1);
            mRecyclerView.scrollToPosition(getItemCount() - 1);
        } else if (state != mFooterItem.getState()) {
            mShowingFooter = true;
            mFooterItem.setState(state);
            notifyItemChanged(getItemCount() - 1);
            mRecyclerView.scrollToPosition(getItemCount() - 1);
        }
    }

    public void hideFooter() {
        if (shouldShowFooter()) {
            mShowingFooter = false;
            notifyItemRemoved(getHeaderSize() + mItemSortList.size() - 1);
        }
    }

    public boolean isLoadingMore() {
        return shouldShowFooter() && mFooterItem.getState() == FooterState.STATE_LOADING;
    }

    private boolean shouldShowHeader() {
        return mHeaderProvider != null && mShowHeader;
    }

    private boolean shouldShowFooter() {
        return mFooterProvider != null && mShowingFooter;
    }

    private boolean supportButNotShowingFooter() {
        return mFooterProvider != null && !mShowingFooter;
    }

    public boolean getSpanSizeLookUpEnable() {
        return mSpanSizeLookUpEnable;
    }

    public void setSpanSizeLookUpEnable(boolean enable) {
        this.mSpanSizeLookUpEnable = enable;
    }

    private boolean shouldSpanSizeLookUp(int totalSpanCount) {
        return mSpanSizeLookUpEnable || ((shouldShowHeader() || shouldShowFooter()) && totalSpanCount != 1);
    }

    public void register(Class<? extends Item> item, ModelFactory factory) {
        mProviderPool.register(item, factory);
    }

    @SuppressWarnings("unchecked")
    public void setItems(List<? extends Item> items) {
        mItemSortList.beginBatchedUpdates();
        try {
            mItemSortList.clear();
            mItemSortList.addAll((Collection<Item>) items);
        } finally {
            mItemSortList.endBatchedUpdates();
        }
    }

    @SuppressWarnings("unchecked")
    public void insertItems(List<? extends Item> items) {
        mItemSortList.beginBatchedUpdates();
        try {
            mItemSortList.addAll((Collection<Item>) items);
        } finally {
            mItemSortList.endBatchedUpdates();
        }
    }

    public <T extends Item> void updateItem(T item) {
        mItemSortList.add(item);
    }

    public <T extends Item> void updateItem(int index, T item) {
        mItemSortList.updateItemAt(index, item);
    }

    public int getVisibleRows() {
        return mVisibleRows;
    }

    public void setVisibleRows(int visibleRows) {
        this.mVisibleRows = visibleRows;
    }

    boolean isListRefreshing() {
        return mCallback == null || mCallback.isListRefreshing();
    }

    private ListAdapterCallback mCallback;

    public interface ListAdapterCallback {
        boolean isListRefreshing();
    }

    private OnLoadMoreListener mListener;

    public OnLoadMoreListener getOnLoadMoreListener() {
        return mListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mListener = listener;
    }

    void dispatchLoadMore() {
        if (mListener != null) {
            mListener.onListLoadMore();
        }
    }

    public interface OnLoadMoreListener {
        void onListLoadMore();
    }
}
