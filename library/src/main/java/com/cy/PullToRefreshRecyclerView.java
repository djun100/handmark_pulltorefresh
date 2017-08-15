package com.cy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.R;

/**
 * @author chz
 * @description
 * @date 2016/3/15 14:10
 */
public class PullToRefreshRecyclerView extends PullToRefreshBase<WrapRecyclerView> {
    public PullToRefreshRecyclerView(Context context) {
        super(context);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshRecyclerView(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
    }

    @Override
    public Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected WrapRecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        WrapRecyclerView recyclerView = new WrapRecyclerView(context, attrs);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(isReadyForPullStart()){
                    recyclerView.clearFocus();
                }
            }
        });
        recyclerView.setId(R.id.pulltorefresh_recyclerview);
        return recyclerView;
    }



    @Override
    protected boolean isReadyForPullEnd() {
        int lastPosition = getRefreshableView().getLastVisiblePosition();
        RecyclerView.LayoutManager layoutManager = getRefreshableView().getLayoutManager();
        View lastView = layoutManager.findViewByPosition(lastPosition);
        if(lastView != null) {
            int lastBottom = lastView.getBottom();
            return lastPosition == getRefreshableView().getCount() - 1 && lastBottom <= getRefreshableView().getBottom();
        }
        else{
            return true;
        }
    }

    @Override
    protected boolean isReadyForPullStart() {
        int firstPosition = getRefreshableView().getFirstVisiblePosition();
        RecyclerView.LayoutManager layoutManager = getRefreshableView().getLayoutManager();
        View firstView = layoutManager.findViewByPosition(firstPosition);
        if(firstView != null) {
            int firstTop = firstView.getTop();
            return firstPosition == 0 && firstTop >= 0;
        }
        else{
            return true;
        }
    }

}
