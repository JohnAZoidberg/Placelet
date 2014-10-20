package net.placelet;

import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

class CompositeScrollListener implements AbsListView.OnScrollListener {
    private List<AbsListView.OnScrollListener> registeredListeners = new ArrayList<AbsListView.OnScrollListener>();

    public void registerListener (AbsListView.OnScrollListener listener) {
        registeredListeners.add(listener);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        for(AbsListView.OnScrollListener listener:registeredListeners) {
            listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
}