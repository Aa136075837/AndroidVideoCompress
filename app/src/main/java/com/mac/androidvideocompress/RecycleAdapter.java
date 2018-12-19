package com.mac.androidvideocompress;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.VirtualLayoutAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;

import java.util.Collections;
import java.util.List;

public class RecycleAdapter extends VirtualLayoutAdapter
    implements View.OnClickListener, RecycleItemHelper.TouckItemCallback {
    private OnRecyItemClickListener mListener;
    private Context mContext;
    private List<LayoutHelper> mHelpers;

    public RecycleAdapter (@NonNull VirtualLayoutManager layoutManager, Context context) {
        super (layoutManager);
        mContext = context;
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        TextView view = new TextView (mContext);
        view.setOnClickListener (this);
        return new ViewHolder (view);
    }

    @Override public void onBindViewHolder (RecyclerView.ViewHolder holder, int position) {
        VirtualLayoutManager.LayoutParams layoutParams =
            new VirtualLayoutManager.LayoutParams (ViewGroup.LayoutParams.MATCH_PARENT, 300);
        holder.itemView.setLayoutParams (layoutParams);
        holder.itemView.setTag (position);

        ((TextView) holder.itemView).setText (Integer.toString (position));

        if (position == 7) {
            layoutParams.height = 120;
            layoutParams.width = 120;
        } else if (position > 35) {
            layoutParams.height = 200 + (position - 30) * 100;
        }

        if (position > 35) {
            holder.itemView.setBackgroundColor (0x66cc0000 + (position - 30) * 128);
        } else if (position % 2 == 0) {
            holder.itemView.setBackgroundColor (0xaa00ff00);
        } else {
            holder.itemView.setBackgroundColor (0xccff00ff);
        }
    }

    @Override public int getItemCount () {
        mHelpers = getLayoutHelpers ();
        if (mHelpers == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0, size = mHelpers.size (); i < size; i++) {
            count += mHelpers.get (i).getItemCount ();
        }
        return count;
    }

    public void setOnRecyItemClickListener (OnRecyItemClickListener listener) {
        mListener = listener;
    }

    @Override public void onClick (View v) {
        if (null != mListener) {
            Log.e ("BO111", "------------listener 不为null");
            mListener.onRecyItemCLick (v, (int) v.getTag ());
        }
    }

    @Override public void onItemDelete (int position) {
        Log.e ("BO222", "position ==== " + position);
        if (position < mHelpers.size () - 1) {
            mHelpers.remove (position);
            setLayoutHelpers (mHelpers);
            notifyItemRemoved (position);
        }
    }

    @Override public void onMove (int fromPosition, int toPosition) {
        Log.e ("BO333", "fromPosition === " + fromPosition + "     toPosition == " + toPosition);
        if (fromPosition < mHelpers.size () - 1 && toPosition < mHelpers.size () - 1) {
            Collections.swap (mHelpers, fromPosition, toPosition);
        }
    }

    public interface OnRecyItemClickListener {
        void onRecyItemCLick (View v, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder (View itemView) {
            super (itemView);
        }
    }
}