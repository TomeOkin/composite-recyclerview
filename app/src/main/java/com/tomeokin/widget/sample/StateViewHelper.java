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

import android.support.annotation.NonNull;
import android.view.View;

import com.kennyc.view.MultiStateView;

public class StateViewHelper {
    private MultiStateView mMultiStateView;
    OnStateChangedListener mListener;

    private final View.OnClickListener mOnRefreshListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dispatchRefresh();
        }
    };
    private final MultiStateView.StateListener mStateListener = new MultiStateView.StateListener() {
        @Override
        public void onStateChanged(@MultiStateView.ViewState int oldState, @MultiStateView.ViewState int newState) {
            if (mListener != null) {
                mListener.onStateChanged(oldState, newState);
            }
        }

        @Override
        public void onStateInflated(int viewState, @NonNull View view) {

        }
    };

    public StateViewHelper(MultiStateView multiStateView) {
        mMultiStateView = multiStateView;
        initView(MultiStateView.VIEW_STATE_EMPTY);
        initView(MultiStateView.VIEW_STATE_ERROR);
        mMultiStateView.setStateListener(mStateListener);
    }

    private void initView(@MultiStateView.ViewState int viewState) {
        View target = mMultiStateView.getView(viewState);
        if (target != null) {
            target.setOnClickListener(mOnRefreshListener);
        }
    }

    void dispatchRefresh() {
        if (mMultiStateView.getViewState() != MultiStateView.VIEW_STATE_CONTENT) {
            mMultiStateView.setViewState(MultiStateView.VIEW_STATE_LOADING);
        }
        if (mListener != null) {
            mListener.onLoadContent();
        }
    }

    public void switchToContent() {
        mMultiStateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
    }

    public void switchToEmpty() {
        mMultiStateView.setViewState(MultiStateView.VIEW_STATE_EMPTY);
    }

    public void switchToError() {
        mMultiStateView.setViewState(MultiStateView.VIEW_STATE_ERROR);
    }

    public OnStateChangedListener getOnStateChangedListener() {
        return mListener;
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mListener = listener;
    }

    public interface OnStateChangedListener {
        void onStateChanged(@MultiStateView.ViewState int oldState, @MultiStateView.ViewState int newState);

        void onLoadContent();
    }
}
