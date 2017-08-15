package com.cy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * @author chz
 * @description
 * @date 2016/3/17 14:38
 */
public class WrapRecyclerView extends RecyclerView {

    private WrapAdapter mWrapAdapter;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public WrapRecyclerView(Context context) {
        super(context);
        init();
    }

    public WrapRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WrapRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mWrapAdapter = new WrapAdapter();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    public void addHeaderView(View header) {
        if (header == null) {
            return;
        }
        mWrapAdapter.addHeaderView(header);
    }

    public void addFooterView(View footer) {
        if (footer == null) {
            return;
        }
        mWrapAdapter.addFooterView(footer);
    }

    public void removeHeaderView(View header) {
        if (header == null) {
            return;
        }
        mWrapAdapter.removeHeaderView(header);
    }

    public void removeFooterView(View footer) {
        if (footer == null) {
            return;
        }
        mWrapAdapter.removeHeaderView(footer);
    }

    public void setSelection(int position) {
        scrollToPosition(position);
    }

    @Override
    public void setLayoutManager(final LayoutManager layout) {
        if (layout instanceof GridLayoutManager) {
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = ((GridLayoutManager) layout).getSpanSizeLookup();
            ((GridLayoutManager) layout).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (mWrapAdapter.isHeader(position) || mWrapAdapter.isFooter(position)) {
                        return ((GridLayoutManager) layout).getSpanCount();
                    }
                    return spanSizeLookup.getSpanSize(position - mWrapAdapter.getHeaderCount());
                }
            });
        }
        super.setLayoutManager(layout);
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        mWrapAdapter.setAdapter(adapter);
        super.swapAdapter(mWrapAdapter, removeAndRecycleExistingViews);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mWrapAdapter.setAdapter(adapter);
        super.setAdapter(mWrapAdapter);
    }

    @Override
    public Adapter getAdapter() {
        return mWrapAdapter.getAdapter();
    }

    public int getCount() {
        return mWrapAdapter.getItemCount();
    }

    public int getFirstVisiblePosition() {
        int firstPosition = 0;
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            firstPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        if (layoutManager instanceof GridLayoutManager) {
            firstPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] positions = null;
            ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(positions);
            firstPosition = positions[0];
        }
        return firstPosition;
    }


    public int getLastVisiblePosition() {
        int lastPosition = 0;
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }
        if (layoutManager instanceof GridLayoutManager) {
            lastPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        }
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] positions = null;
            ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(positions);
            lastPosition = positions[positions.length - 1];
        }
        return lastPosition;
    }


    class WrapAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final static int TYPE_HEADER = -1000;
        private final static int TYPE_FOOTER = -2000;
        private RecyclerView.Adapter mAdapter;
        private ArrayList<View> mHeaderViews;
        private ArrayList<View> mFooterViews;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder holder = null;
            int type = 0;
            int position = 0;
            if (viewType <= TYPE_FOOTER) {
                type = TYPE_FOOTER;
                position = TYPE_FOOTER - viewType;
            } else if (viewType <= TYPE_HEADER) {
                type = TYPE_HEADER;
                position = TYPE_HEADER - viewType;
            } else {
                type = viewType;
            }
            switch (type) {
                case TYPE_HEADER:
                    View header = mHeaderViews.get(position);
                    setWrapParems(header);
                    holder = new WrapViewHolder(header);
                    break;
                case TYPE_FOOTER:
                    View footer = mFooterViews.get(position);
                    setWrapParems(footer);
                    holder = new WrapViewHolder(footer);
                    break;
                default:
                    holder = mAdapter.onCreateViewHolder(parent, viewType);
                    break;
            }
            return holder;
        }

        private void setWrapParems(View view) {
            int width = getLayoutManager().canScrollVertically() ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = getLayoutManager().canScrollVertically() ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
            if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager.LayoutParams headerParams = new StaggeredGridLayoutManager.LayoutParams(width, height);
                headerParams.setFullSpan(true);
                view.setLayoutParams(headerParams);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder instanceof WrapViewHolder) {
            } else {
                int realPosition = position - getHeaderCount();
                mAdapter.onBindViewHolder(holder, realPosition);
                holder.itemView.setOnClickListener(new OnPositionClick(realPosition));
                holder.itemView.setOnLongClickListener(new OnPositionLongClick(realPosition));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position < getHeaderCount()) {
                return TYPE_HEADER - position;
            } else if (position < getItemCount() - getFooterCount()) {
                return mAdapter.getItemViewType(position - getHeaderCount());
            } else {
                return TYPE_FOOTER - position + getItemCount() - getFooterCount();
            }
        }

        public boolean isHeader(int position) {
            return position < getHeaderCount();
        }

        public boolean isFooter(int position) {
            return position >= getItemCount() - getFooterCount();
        }

        public void setAdapter(Adapter adapter) {
            mAdapter = adapter;
            mAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    notifyDataSetChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    notifyItemRangeChanged(positionStart, itemCount);
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    notifyItemRangeInserted(positionStart, itemCount);
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    notifyItemRangeRemoved(positionStart, itemCount);
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    notifyItemMoved(fromPosition, toPosition);
                }
            });
            notifyDataSetChanged();
        }

        public Adapter getAdapter() {
            return mAdapter;
        }

        public void addHeaderView(View header) {
            if (mHeaderViews == null) {
                mHeaderViews = new ArrayList<View>();
            }
            if (!mHeaderViews.contains(header)) {
                mHeaderViews.add(header);
            }
            notifyDataSetChanged();
        }

        public void removeHeaderView(View header) {
            if (mHeaderViews != null) {
                mHeaderViews.remove(header);
                notifyDataSetChanged();
            }
        }

        public int getHeaderCount() {
            if (mHeaderViews == null) {
                return 0;
            }
            return mHeaderViews.size();
        }

        public void addFooterView(View footer) {
            if (mFooterViews == null) {
                mFooterViews = new ArrayList<View>();
            }
            if (!mFooterViews.contains(footer)) {
                mFooterViews.add(footer);
            }
            notifyDataSetChanged();
        }

        public void removeFooterView(View footer) {
            if (mFooterViews != null) {
                mFooterViews.remove(footer);
                notifyDataSetChanged();
            }
        }

        public int getFooterCount() {
            if (mFooterViews == null) {
                return 0;
            }
            return mFooterViews.size();
        }

        @Override
        public int getItemCount() {
            int count = getHeaderCount() + getFooterCount();
            if (mAdapter != null) {
                count += mAdapter.getItemCount();
            }
            return count;
        }

        class WrapViewHolder extends ViewHolder {
            public WrapViewHolder(View itemView) {
                super(itemView);
            }
        }

        class OnPositionClick implements View.OnClickListener {
            private int position;

            public OnPositionClick(int position) {
                this.position = position;
            }

            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        }

        class OnPositionLongClick implements View.OnLongClickListener {
            private int position;

            public OnPositionLongClick(int position) {
                this.position = position;
            }

            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(v, position);
                    return true;
                }
                return false;
            }
        }
    }

    public void setTransparentDivider(final int sizePx) {
        addItemDecoration(new ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, State state) {
                super.onDrawOver(c, parent, state);
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
                int position = ((LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
                int index = position - mWrapAdapter.getHeaderCount();

                if (index < 0) {
                    super.getItemOffsets(outRect, view, parent, state);
                    return;
                }

                if (isEnd(parent, index)) {
                    if (isVertical(parent)) {
                        outRect.set(0, 0, 0, sizePx);
                    } else {
                        outRect.set(0, 0, sizePx, 0);
                    }
                } else {
                    outRect.set(0, 0, sizePx, sizePx);
                }
            }

            private boolean isVertical(RecyclerView parent) {
                LayoutManager layout = parent.getLayoutManager();
                if (layout instanceof StaggeredGridLayoutManager) {
                    return ((StaggeredGridLayoutManager) layout).getOrientation() == StaggeredGridLayoutManager.VERTICAL;
                } else if (layout instanceof LinearLayoutManager) {
                    return ((LinearLayoutManager) layout).getOrientation() == LinearLayoutManager.VERTICAL;
                }
                return true;
            }

            private int getSpanCount(RecyclerView parent) {
                LayoutManager layout = parent.getLayoutManager();
                if (layout instanceof GridLayoutManager) {
                    return ((GridLayoutManager) layout).getSpanCount();
                }
                if (layout instanceof StaggeredGridLayoutManager) {
                    return ((StaggeredGridLayoutManager) layout).getSpanCount();
                }
                return 1;
            }

            private boolean isEnd(RecyclerView parent, int index) {
                int spanCount = getSpanCount(parent);
                return (index + 1) % spanCount == 0;
            }
        });
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        public void onItemLongClick(View view, int position);
    }
}
