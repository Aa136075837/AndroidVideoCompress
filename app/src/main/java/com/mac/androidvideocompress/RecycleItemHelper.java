package com.mac.androidvideocompress;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class RecycleItemHelper extends ItemTouchHelper.Callback {
    private TouckItemCallback mTouckItemCallback;

    public RecycleItemHelper (TouckItemCallback touckItemCallback) {
        mTouckItemCallback = touckItemCallback;
    }

    @Override
    public int getMovementFlags (RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags (ItemTouchHelper.UP| ItemTouchHelper.DOWN,ItemTouchHelper.END);
    }

    @Override public boolean onMove (RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
        RecyclerView.ViewHolder target) {
        mTouckItemCallback.onMove (viewHolder.getAdapterPosition (), target.getAdapterPosition ());
        return true;
    }

    @Override public void onSwiped (RecyclerView.ViewHolder viewHolder, int direction) {
        mTouckItemCallback.onItemDelete (viewHolder.getAdapterPosition ());
    }

    @Override public void onSelectedChanged (RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged (viewHolder, actionState);
    }

    public interface TouckItemCallback {
        void onItemDelete (int position);

        void onMove (int fromPosition, int toPosition);
    }
}