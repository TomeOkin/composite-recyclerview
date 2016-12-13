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

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kennyc.view.MultiStateView;
import com.tomeokin.widget.FooterState;
import com.tomeokin.widget.ListAdapter;
import com.tomeokin.widget.ListViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListFragment extends Fragment {
    StateViewHelper mStateViewHelper;
    ListViewHolder mListViewHolder;
    ListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        MultiStateView multiStateView = (MultiStateView) inflater.inflate(R.layout.fragment_state, container, false);
        mStateViewHelper = new StateViewHelper(multiStateView);
        mListViewHolder = new ListViewHolder(inflater, container, new LinearLayoutManager(getContext()));
        multiStateView.addView(mListViewHolder.getRootView());
        mStateViewHelper.setOnStateChangedListener(new StateViewHelper.OnStateChangedListener() {
            @Override
            public void onStateChanged(@MultiStateView.ViewState int oldState, @MultiStateView.ViewState int newState) {
                mListViewHolder.resetViewState();
            }

            @Override
            public void onLoadContent() {
                mListViewHolder.dispatchRefresh();
            }
        });

        mListViewHolder.setOnListChangeListener(new ListViewHolder.OnListChangeListener() {
            @Override
            public void onListRefresh() {
                onRefresh();
            }

            @Override
            public void onListLoadMore() {
                onLoadMore();
            }
        });
        mAdapter = mListViewHolder.getListAdapter();
        mAdapter.register(Cat.class, new CatModel.Provider(0));
        mAdapter.setFooterProvider(new ToastFooterModel.Provider(Integer.MAX_VALUE, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListViewHolder.dispatchLoadMore();
            }
        }), new FooterState(FooterState.STATE_LOADING));
        mListViewHolder.getRecyclerView().setHasFixedSize(true);

        mListViewHolder.onAttach();
        return multiStateView;
    }

    protected void onRefresh() {
        index = 0;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("Adapter", "onRefresh");
                List<Cat> cats = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    Cat cat = new Cat(String.format(Locale.ENGLISH, "%03d", i),
                        Character.toString((char) ('a' + i)) + " - " + Character.toString((char) ('o' + i)));
                    Log.i("Cat", cat.toString());
                    cats.add(cat);
                }
                mAdapter.setItems(cats);
                mAdapter.hideFooter();
                mStateViewHelper.switchToContent();
            }
        }, 2000);
    }

    int index = 0;

    protected void onLoadMore() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("Adapter", "onLoadMore");

                if (index == 0 || index == 2) {
                    List<Cat> cats = new ArrayList<>();
                    for (int i = mAdapter.getNormalItemCount(); i < 20; i++) {
                        Cat cat = new Cat(String.format(Locale.ENGLISH, "%03d", i),
                            Character.toString((char) ('a' + i)) + " - " + Character.toString((char) ('o' + i)));
                        Log.i("Cat", cat.toString());
                        cats.add(cat);
                    }
                    mAdapter.insertItems(cats);
                    mAdapter.hideFooter();

                    if (index == 2) {
                        mListViewHolder.setHasMore(false);
                    }
                } else if (index == 1) {
                    mAdapter.showFooter(FooterState.STATE_ERROR);
                }

                index++;
            }
        }, 3000);
    }
}
