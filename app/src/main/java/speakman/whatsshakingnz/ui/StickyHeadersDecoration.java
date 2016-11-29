/*
 * Copyright 2016 Adam Speakman
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

package speakman.whatsshakingnz.ui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adam on 2016-11-26.
 */

public class StickyHeadersDecoration extends RecyclerView.ItemDecoration {

    interface StickyHeadersAdapter {
        long getSectionId(int position);

        @Nullable
        View getHeaderViewForSection(long section, RecyclerView parent);
    }

    private StickyHeadersAdapter adapter;
    private Map<Long, View> headers;

    public StickyHeadersDecoration(@NonNull StickyHeadersAdapter adapter) {
        this.adapter = adapter;
        this.headers = new HashMap<>();
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        final int left = parent.getPaddingLeft();
        long currentSection = -1;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int adapterPosition = parent.getChildAdapterPosition(child);
            long section = adapter.getSectionId(adapterPosition);
            if (section != currentSection) {
                currentSection = section;
                View headerView = getViewForSection(currentSection, parent);
                int headerHeight = headerView.getMeasuredHeight();
                int top;
                // Is this header about to get pushed off the screen?
                int nextItem = adapterPosition + 1;
                if (nextItem < parent.getAdapter().getItemCount() // Current item is not last item
                        && adapter.getSectionId(nextItem) != currentSection // Next is different section
                        && child.getBottom() < headerHeight) { // Next has pushed up under current
                    top = child.getBottom() - headerHeight;
                } else {
                    top = Math.max(0, parent.getLayoutManager().getDecoratedTop(child));
                }
                headerView.layout(left, top, left + headerView.getMeasuredWidth(), top + headerView.getMeasuredHeight());
                headerView.setAlpha(child.getAlpha());

                c.save();
                c.translate(left, top);
                headerView.draw(c);
                c.restore();
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View child, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, child, parent, state);
        int adapterPosition = parent.getChildAdapterPosition(child);
        long section = adapter.getSectionId(adapterPosition);
        // Need to apply offsets for this child if it is first, or first in section
        if (adapterPosition == 0 || section != adapter.getSectionId(adapterPosition - 1)) {
            View headerView = getViewForSection(section, parent);
            outRect.set(0, headerView.getMeasuredHeight(), 0, 0);
        }
    }

    public void notifyDataSetChanged() {
        headers.clear();
    }

    private View getViewForSection(long section, RecyclerView parent) {
        View headerView;
        if (headers.containsKey(section)) {
            headerView = headers.get(section);
        } else {
            headerView = adapter.getHeaderViewForSection(section, parent);
            headerView.measure(headerView.getLayoutParams().width, headerView.getLayoutParams().height);
            headers.put(section, headerView);
        }
        return headerView;
    }
}
