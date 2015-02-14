package in.org.amnesty.outreach.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class RecyclerPlusView extends RecyclerView {

    private final AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };
    private View emptyView;

    public RecyclerPlusView(Context context) { super(context); }

    public RecyclerPlusView(Context context, AttributeSet attrs) { super(context, attrs); }

    public RecyclerPlusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void checkIfEmpty() {
        if (emptyView != null && getAdapter() != null) {
            emptyView.setVisibility(getAdapter().getItemCount() > 0 ? GONE : VISIBLE);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
        checkIfEmpty();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (null != emptyView && (visibility == GONE || visibility == INVISIBLE)) {
            emptyView.setVisibility(GONE);
        } else {
            checkIfEmpty();
        }
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }
}